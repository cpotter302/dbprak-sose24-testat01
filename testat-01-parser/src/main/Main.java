package main;

import main.entities.*;
import main.logger.DBLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * The Main class serves as the entry point for the application.
 * It is responsible for parsing XML and CSV data files, inserting the data into the database,
 * and performing various data processing tasks.
 */
public class Main {

    private static final String XML_DATA_DRESDEN = Helpers.loadEnv("DATA_DRESDEN_PATH");
    private static final String XML_DATA_LEIPZIG = Helpers.loadEnv("DATA_LEIPZIG_PATH");
    private static final String XML_DATA_CATEGORIES = Helpers.loadEnv("DATA_CATEGORIES_PATH");
    private static final String CSV_DATA_REVIEWS = Helpers.loadEnv("DATA_REVIEWS_PATH");

    private static final Map<String, String[]> productSimilars = new HashMap<>();

    /**
     * The main method which serves as the entry point of the application.
     *
     * @param args command-line arguments
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     * @throws IOException if an I/O error occurs
     * @throws SAXException if any parse errors occur
     * @throws SQLException if a database access error occurs
     */
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, SQLException {
        final Connection con = DBConnector.connect();
        assert con != null;

        // Parse data Dresden
        DBLogger.LOG.info("Parsing XML from file -> " + XML_DATA_DRESDEN);
        parseXML(con, XML_DATA_DRESDEN, "Dresden");

        // Parse data Leipzig
        DBLogger.LOG.info("Parsing XML from file -> " + XML_DATA_LEIPZIG);
        parseXML(con, XML_DATA_LEIPZIG, "Leipzig");

        // Parse categories
        DBLogger.LOG.info("Parsing XML from file -> " + XML_DATA_CATEGORIES);
        parseCategoriesXML(con);

        // Parse reviews
        DBLogger.LOG.info("Parsing CSV from file -> " + CSV_DATA_REVIEWS);
        parseCSV(con);

        // Wait for all Products to be created then create similar products
        createSimiliarProducts(con);

        // Calculate average ratings
        Product.calculateAverageRatings(con);

        // Write log records to file
        DBLogger.writeRejectedRecords();
    }

    /**
     * Parses the categories XML file and inserts the data into the database.
     *
     * @param con the database connection
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     * @throws IOException if an I/O error occurs
     * @throws SAXException if any parse errors occur
     */
    private static void parseCategoriesXML(Connection con) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new File(XML_DATA_CATEGORIES));
        doc.getDocumentElement().normalize();

        // Get root category
        NodeList categoryNodes = doc.getElementsByTagName("categories").item(0).getChildNodes();
        for (int i = 0; i < categoryNodes.getLength(); i++) {
            Node categoryNode = categoryNodes.item(i);
            parseCategory(con, categoryNode, null);
        }
    }

    /**
     * Parses a category node and inserts it into the database.
     *
     * @param con the database connection
     * @param categoryNode the category node to be parsed
     * @param parentID the ID of the parent category, if any
     */
    private static void parseCategory(Connection con, Node categoryNode, Integer parentID) {
        if (categoryNode.getNodeType() == Node.ELEMENT_NODE) {
            Element categoryElement = (Element) categoryNode;
            String categoryName = categoryElement.getTextContent().split("\n")[0].trim();

            // Create category
            Category category = new Category(categoryName, parentID);
            if (category.checkForCategoryNameAndParent(con)) {
                DBLogger.logRejectedRecord("CATEGORY", "name", "Category already exists: " + categoryName);
                return;
            }
            category.createCategory(con);

            NodeList categorieChildNodes = categoryElement.getChildNodes();
            for (int j = 0; j < categorieChildNodes.getLength(); j++) {
                Node currentNode = categorieChildNodes.item(j);
                if (currentNode.getNodeName().equals("item")) {
                    // Create Product Category
                    String itemName = currentNode.getTextContent().trim();
                    Product product = new Product(itemName);
                    if (product.checkForProductID(con)) {
                        ProductCategory productCategory = new ProductCategory(itemName, category.getCategoryId());
                        productCategory.createCategory(con);
                    } else {
                        DBLogger.logRejectedRecord("PRODUCT_CATEGORY", "product_id", "Product does not exist: " + itemName);
                    }
                } else if (currentNode.getNodeName().equals("category")) {
                    parseCategory(con, currentNode, category.getCategoryId());
                }
            }
        }
    }

    /**
     * Parses the reviews CSV file and inserts the data into the database.
     *
     * @param con the database connection
     */
    private static void parseCSV(Connection con) {
        String line;
        String csvSplitBy = "\",\"";

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_DATA_REVIEWS))) {
            // Skip the header line if present
            br.readLine();

            ArrayList<Review> reviews = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                if (data.length >= 4) {
                    String productID = data[0].replace("\"", "");
                    String rating = data[1];
                    String customerID = data[4];

                    // Handle customer
                    Customer customer = new Customer(customerID);
                    if (!customer.checkForCustomerID(con)) {
                        customer.createCustomer(con);
                    }

                    // Handle review
                    Product product = new Product(productID);
                    boolean productExists = product.checkForProductID(con);

                    if (Integer.parseInt(rating) < 1 || Integer.parseInt(rating) > 5) {
                        DBLogger.logRejectedRecord("REVIEW", "rating", "Rating out of bounds: " + rating + " for product: " + productID);
                        continue;
                    }

                    Review review = new Review(customer, Integer.parseInt(rating), product.getProductId());
                    review.setDescription(data[6]);

                    // Create review only if product exists
                    if (productExists) {
                        reviews.add(review);
                    } else {
                        DBLogger.logRejectedRecord("REVIEW", "productID", "Product does not exist: " + productID);
                    }
                } else {
                    DBLogger.logRejectedRecord("REVIEW", "-", "Invalid CSV line: " + line);
                }
            }

            // Wait for all customers/products to be created, then create reviews
            for (Review review : reviews) {
                review.createReview(con);
            }
        } catch (IOException e) {
            DBLogger.LOG.severe("Error reading CSV file: " + e.getMessage());
        }
    }

    /**
     * Parses an XML file and inserts the data into the database.
     *
     * @param con the database connection
     * @param xmlFile the path to the XML file
     * @param cityShop the city shop name (either "Dresden" or "Leipzig")
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     * @throws IOException if an I/O error occurs
     * @throws SAXException if any parse errors occur
     */
    private static void parseXML(Connection con, String xmlFile, String cityShop) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new File(xmlFile));
        doc.getDocumentElement().normalize();

        // Get first shop item
        Node shop = Helpers.getFirstNodeByTagName(doc, "shop");
        if (shop == null) {
            DBLogger.logRejectedRecord("SHOP", "shop", "tag missing: shop");
            return;
        }

        // Get Attributes
        var city = Helpers.getAttributeValue(shop, "name");
        var street = Helpers.getAttributeValue(shop, "street");
        var zip = Helpers.getAttributeValue(shop, "zip");

        Shop newShop = new Shop(street, zip, city);
        newShop.createShop(con);

        // Parse items
        NodeList itemList = shop.getChildNodes();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node currentItem = itemList.item(i);
            if (currentItem.getNodeType() == Node.ELEMENT_NODE) {
                String product_id = Helpers.getAttributeValue(currentItem, "asin");
                if (product_id == null) {
                    DBLogger.logRejectedRecord("PRODUCT", "asin", "attribute missing: asin");
                    continue;
                }

                Product newProduct = new Product(product_id);

                // Check if the reffered Product in this offer exists in the Database.
                if (!newProduct.checkForProductID(con)) {
                    // If not -> Create Product and Type Information

                    String salesrank = Helpers.getAttributeValue(currentItem, "salesrank");
                    if (salesrank == null || salesrank.isEmpty()) {
                        DBLogger.logRejectedRecord("PRODUCT", "salesrank", "attribute missing: salesrank " + newProduct.getProductId());
                        continue;
                    }
                    Element itemElement = (Element) currentItem;
                    var node = Helpers.getFirstNodeByTagName(itemElement, "title");
                    if (node == null) {
                        DBLogger.logRejectedRecord("PRODUCT", "title", "tag missing: title " + newProduct.getProductId());
                        continue;
                    }
                    String title = Helpers.getInnerText(node);

                    node = Helpers.getFirstNodeByTagName(itemElement, "details");
                    String imageUrl;
                    if (node != null) {
                        imageUrl = Helpers.getInnerText(node).trim();
                    } else {
                        imageUrl = Helpers.getAttributeValue(currentItem, "picture");
                    }

                    String productGroup = Helpers.getAttributeValue(currentItem, "pgroup");
                    if (productGroup == null) {
                        DBLogger.logRejectedRecord("PRODUCT", "pgroup", "attribute missing: pgroup " + newProduct.getProductId());
                        continue;
                    }

                    newProduct.setTitle(title);
                    newProduct.setSalesrank(Integer.parseInt(salesrank));
                    newProduct.setImage(imageUrl);
                    newProduct.setProductGroup(productGroup);

                    // Check product type

                    String productType = Helpers.getAttributeValue(currentItem, "pgroup");
                    if (productType == null) continue;

                    switch (productType) {
                        case "Book":
                            // Create Book
                            if (!createBookProduct(itemElement, newProduct, con, cityShop)) continue;
                            if (!createOfferWrapper(itemElement, con, newProduct, newShop)) continue;
                            break;
                        case "DVD":
                            // Create DVD
                            if (!createDVDProduct(itemElement, newProduct, con, cityShop)) continue;
                            if (!createOfferWrapper(itemElement, con, newProduct, newShop)) continue;
                            break;
                        case "Music":
                            // Create CD
                            if (!createCDProduct(itemElement, newProduct, con, cityShop)) continue;
                            if (!createOfferWrapper(itemElement, con, newProduct, newShop)) continue;
                            break;
                        default:
                            DBLogger.logRejectedRecord("PRODUCT", "pgroup", "Unknown product type: " + productType);
                            break;
                    }
                } else {
                    DBLogger.LOG.info("Product already exists");
                }
            }

        }
    }

    /**
     * Creates a book product from the provided XML element.
     *
     * @param itemElement The XML element containing the book product information.
     * @param newProduct  The Product object representing the book product.
     * @param con         The database connection.
     * @param cityShop    The city where the shop is located.
     * @return True if the book product is successfully created, false otherwise.
     */
    private static boolean createBookProduct(Element itemElement, Product newProduct, Connection con, String cityShop) {
        // Create Book
        //Get Pages
        Node node = Helpers.getFirstNodeByTagName(itemElement, "pages");
        if (node == null) {
            return false;
        }
        var pages = Helpers.getInnerText(node);
        if (pages.isEmpty()) {
            DBLogger.logRejectedRecord("BOOK", "pages", "text missing: pages " + newProduct.getProductId());
            return false;
        }

        //Get publishDate
        node = Helpers.getFirstNodeByTagName(itemElement, "publication");
        if (node == null) {
            DBLogger.logRejectedRecord("BOOK", "publication", "tag missing: publication " + newProduct.getProductId());
            return false;
        }
        var publishDate = Helpers.getAttributeValue(node, "date");
        if (publishDate == null || publishDate.isEmpty()) {
            DBLogger.logRejectedRecord("BOOK", "publication", "attribute missing: date " + newProduct.getProductId());
            return false;
        }

        //Get isbn
        var isbnAttr = itemElement.getElementsByTagName("isbn").item(0).getAttributes();
        if (isbnAttr == null) {
            DBLogger.logRejectedRecord("BOOK", "isbn", "tag missing: isbn " + newProduct.getProductId());
            return false;
        }

        var isbn = isbnAttr.getNamedItem("val").getNodeValue();
        if (isbn == null) {
            DBLogger.logRejectedRecord("BOOK", "isbn", "attribute missing: val " + newProduct.getProductId());
            return false;
        }

        //Get publisher
        var publisherItem = itemElement.getElementsByTagName("publisher").item(0);
        if (publisherItem == null) {
            DBLogger.logRejectedRecord("BOOK", "publisher", "tag missing: publisher " + newProduct.getProductId());
            return false;
        }

        var publisherTextContent = publisherItem.getTextContent();
        var publisherAttributeValue = Helpers.getAttributeValue(publisherItem, "name");

        String publisher;
        if (publisherTextContent.isEmpty() && (publisherAttributeValue == null || publisherAttributeValue.isEmpty())) {
            DBLogger.logRejectedRecord("BOOK", "publisher", "attribute missing: name " + newProduct.getProductId());
            return false;
        } else {
            if (publisherTextContent.isEmpty()) {
                publisher = publisherAttributeValue;
            } else {
                publisher = publisherTextContent;
            }
        }

        //If all attributes are present, create the product
        newProduct.createProduct(con);
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date parsedPublishedDate = Date.from(LocalDate.parse(publishDate).atStartOfDay(defaultZoneId).toInstant());
        Book newBook = new Book(Integer.parseInt(pages), new java.sql.Date(parsedPublishedDate.getTime()), isbn, publisher, newProduct.getProductId());
        newBook.createBook(con);

        // Add similar Products after product and book are created
        addSimilarProducts(itemElement, newProduct.getProductId(), cityShop);


        var authorsList = itemElement.getElementsByTagName("author");
        var authorListLength = authorsList.getLength();

        Node currentAuthor;
        for (int j = 0; j < authorListLength; j++) {
            currentAuthor = authorsList.item(j);
            if (currentAuthor.getNodeType() == Node.ELEMENT_NODE) {
                var authorTextContent = currentAuthor.getTextContent();
                var authorAttributeValue = Helpers.getAttributeValue(currentAuthor, "name");
                String author;
                if (authorTextContent.isEmpty() && (authorAttributeValue == null || authorAttributeValue.isEmpty())) {
                    DBLogger.logRejectedRecord("BOOK", "author", "attribute missing: name " + newProduct.getProductId());
                } else {
                    if (publisherTextContent.isEmpty()) {
                        author = authorAttributeValue;
                    } else {
                        author = authorTextContent;
                    }
                    Person person = new Person(author);
                    if (person.checkForPersonID(con)) {
                        //Person exists -> get ID
                        person.setPersonId(person.getPersonID(con));
                    } else {
                        //Person not existent -> create
                        person.createPerson(con);
                        person.setPersonId(person.getPersonID(con));
                    }

                    // Create Book Person Connection
                    ObjectPerson bookPerson = new ObjectPerson(newBook.getBookId(), person.getPersonId());
                    bookPerson.createBookPerson(con);

                }
            }
        }

        return true;
    }

    /**
     * Creates a DVD product from the provided XML element.
     *
     * @param itemElement The XML element containing the DVD product information.
     * @param newProduct  The Product object representing the DVD product.
     * @param con         The database connection.
     * @param cityShop    The city where the shop is located.
     * @return True if the DVD product is successfully created, false otherwise.
     */
    private static boolean createDVDProduct(Element itemElement, Product newProduct, Connection con, String cityShop) {
        // Create DVD
        //Get Format
        Node node = Helpers.getFirstNodeByTagName(itemElement, "format");
        if (node == null) {
            DBLogger.logRejectedRecord("DVD", "format", "tag missing: format " + newProduct.getProductId());
            return false;
        }
        var format = Helpers.getInnerText(node);
        if (format.isEmpty()) {
            DBLogger.logRejectedRecord("DVD", "format", "text missing: format " + newProduct.getProductId());
            return false;
        }

        //Get runningTime
        node = Helpers.getFirstNodeByTagName(itemElement, "runningtime");
        if (node == null) {
            DBLogger.logRejectedRecord("DVD", "runningtime", "tag missing: runningtime " + newProduct.getProductId());
            return false;
        }
        var runningTime = Helpers.getInnerText(node);
        if (runningTime.isEmpty()) {
            DBLogger.logRejectedRecord("DVD", "runningtime", "text missing: runningtime " + newProduct.getProductId());
            return false;
        }

        //Get regionCode
        node = Helpers.getFirstNodeByTagName(itemElement, "regioncode");
        if (node == null) {
            DBLogger.logRejectedRecord("DVD", "regioncode", "tag missing: regioncode " + newProduct.getProductId());
            return false;
        }
        var regionCode = Helpers.getInnerText(node);
        if (regionCode.isEmpty()) {
            DBLogger.logRejectedRecord("DVD", "regioncode", "text missing: regioncode " + newProduct.getProductId());
            return false;
        }

        //If all attributes are present, create the product
        newProduct.createProduct(con);
        DVD newDVD = new DVD(format, Integer.parseInt(runningTime), regionCode, newProduct.getProductId());
        newDVD.createDVD(con);

        // Add similar Products after product and dvd are created
        addSimilarProducts(itemElement, newProduct.getProductId(), cityShop);

        //Get Actors, Creators, Directors
        Person.createObjectPersons(con, "actor", itemElement, newDVD, null);
        Person.createObjectPersons(con, "creator", itemElement, newDVD, null);
        Person.createObjectPersons(con, "director", itemElement, newDVD, null);

        return true;
    }

    /**
     * Creates a CD product from the provided XML element.
     *
     * @param itemElement The XML element containing the CD product information.
     * @param newProduct  The Product object representing the CD product.
     * @param con         The database connection.
     * @param cityShop    The city where the shop is located.
     * @return True if the CD product is successfully created, false otherwise.
     */
    private static boolean createCDProduct(Element itemElement, Product newProduct, Connection con, String cityShop) {
        Node node = Helpers.getFirstNodeByTagName(itemElement, "label");
        if (node == null) {
            DBLogger.logRejectedRecord("CD", "label", "tag missing: label " + newProduct.getProductId());
            return false;
        }

        var labelTextContent = node.getTextContent();
        var labelAttributeValue = Helpers.getAttributeValue(node, "name");
        String label;
        if (labelTextContent.isEmpty() && (labelAttributeValue == null || labelAttributeValue.isEmpty())) {
            DBLogger.logRejectedRecord("CD", "label", "attribute missing: name " + newProduct.getProductId());
            return false;
        } else {
            if (labelTextContent.isEmpty()) {
                label = labelAttributeValue;
            } else {
                label = labelTextContent;
            }
        }

        //Get publish Date

        node = Helpers.getFirstNodeByTagName(itemElement, "releasedate");
        if (node == null) {
            DBLogger.logRejectedRecord("CD", "releasedate", "tag missing: releasedate " + newProduct.getProductId());
            return false;
        }
        var publishDate = Helpers.getInnerText(node);
        if (publishDate == null || publishDate.isEmpty()) {
            DBLogger.logRejectedRecord("CD", "releasedate", "text missing: releasedate " + newProduct.getProductId());
            return false;
        }


        //If all attributes are present, create the product
        newProduct.createProduct(con);
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date parsedPublishedDate = Date.from(LocalDate.parse(publishDate).atStartOfDay(defaultZoneId).toInstant());
        CD newCD = new CD(newProduct.getProductId(), label, new java.sql.Date(parsedPublishedDate.getTime()));
        newCD.createCD(con);

        // Add similar Products after product and cd are created
        addSimilarProducts(itemElement, newProduct.getProductId(), cityShop);

        var tracksElement = Helpers.getFirstNodeByTagName(itemElement, "tracks");
        if (tracksElement == null) {
            DBLogger.logRejectedRecord("CD", "tracks", "tag missing: tracks " + newProduct.getProductId());
            return false;
        }
        var trackList = tracksElement.getChildNodes();
        var trackListLength = trackList.getLength();

        Node currentTrack;
        for (int j = 0; j < trackListLength; j++) {
            currentTrack = trackList.item(j);
            if (currentTrack.getNodeType() == Node.ELEMENT_NODE) {
                var trackTextContent = currentTrack.getTextContent();
                if (trackTextContent.isEmpty()) {
                    DBLogger.logRejectedRecord("CD", "track", "text missing: track " + newProduct.getProductId() + " at position " + j);
                } else {
                    Title newTitle = new Title(trackTextContent.replaceAll("&quot;", ""), newCD.getCdId());
                    newTitle.createTitle(con);
                }
            }
        }

        Person.createObjectPersons(con, "creator", itemElement, null, newCD);
        Person.createObjectPersons(con, "artist", itemElement, null, newCD);

        return true;
    }

    /**
     * Creates similar products for the provided product ID.
     *
     * @param con The database connection.
     */
    private static void createSimiliarProducts(Connection con) {
        for (var entry : productSimilars.entrySet()) {
            String productID = entry.getKey();
            String[] similars = entry.getValue();

            ProductSimilars productSimilars;
            for (String similarID : similars) {
                productSimilars = new ProductSimilars(productID, similarID);
                if (productSimilars.checkForProductSimilar(con)) {
                    productSimilars.createProductSimilar(con);
                } else {
                    var message = String.format("Key (similar_product)=(%s) is not present in table Product.", similarID);
                    DBLogger.logRejectedRecord("PRODUCT_SIMILARS", "product_id", message);
                }
            }
        }
    }

    /**
     * Creates similar products based on the city and adds them to the productSimilars map.
     *
     * @param itemElement The XML element containing product information.
     * @param productId   The ID of the product.
     * @param city        The city where the shop is located.
     */
    private static void addSimilarProducts(Element itemElement, String productId, String city) {
        String[] asinArray;
        switch (city) {
            case "Leipzig":
                asinArray = getSimilarsLeipzig(itemElement);
                if (asinArray.length != 0) {
                    productSimilars.put(productId, asinArray);
                }
                break;
            case "Dresden":
                asinArray = getSimilarsDresden(itemElement);
                if (asinArray.length != 0) {
                    productSimilars.put(productId, asinArray);
                }
                break;
        }
    }

    /**
     * Creates similar products for the provided product ID based on the city of the shop.
     *
     * @param itemElement The XML element containing product information.
     * @return An array of ASINs representing similar products in Leipzig.
     */
    private static String[] getSimilarsLeipzig(Element itemElement) {
        var similarsList = itemElement.getElementsByTagName("sim_product");
        var similarsListLength = similarsList.getLength();
        String[] asinArray = new String[similarsListLength];

        for (int j = 0; j < similarsListLength; j++) {
            Element simProduct = (Element) similarsList.item(j);
            if (simProduct.getNodeType() == Node.ELEMENT_NODE) {
                Element asinElement = (Element) simProduct.getElementsByTagName("asin").item(0);
                String asin = asinElement.getTextContent();
                if (asin != null && !asin.isEmpty()) {
                    asinArray[j] = asin;
                }
            }
        }
        return asinArray;
    }

    /**
     * Creates similar products for the provided product ID based on the city of the shop.
     *
     * @param itemElement The XML element containing product information.
     * @return An array of ASINs representing similar products in Dresden.
     */
    private static String[] getSimilarsDresden(Element itemElement) {
        var similarsList = itemElement.getElementsByTagName("item");
        var similarsListLength = similarsList.getLength();
        String[] asinArray = new String[similarsListLength];

        for (int j = 0; j < similarsListLength; j++) {
            Element simProduct = (Element) similarsList.item(j);
            if (simProduct.getNodeType() == Node.ELEMENT_NODE) {
                String asin = Helpers.getAttributeValue(simProduct, "asin");
                if (asin != null && !asin.isEmpty()) {
                    asinArray[j] = asin;
                }
            }
        }
        return asinArray;
    }

    /**
     * Creates an offer wrapper for the provided product.
     *
     * @param itemElement The XML element containing offer information.
     * @param con         The database connection.
     * @param newProduct  The Product object for which the offer is created.
     * @param newShop     The Shop object representing the shop offering the product.
     * @return True if the offer is successfully created, false otherwise.
     */
    private static boolean createOfferWrapper(Element itemElement, Connection con, Product newProduct, Shop newShop) {
        // Create offer after product and type information are created
        var node = Helpers.getFirstNodeByTagName(itemElement, "price");
        if (node == null) {
            DBLogger.logRejectedRecord("OFFER", "price", "tag missing: price " + newProduct.getProductId());
            return false;
        }

        var state = Helpers.getAttributeValue(node, "state");
        var priceString = Helpers.getInnerText(node);
        Float price = null;
        if (!(priceString == null || priceString.isEmpty())) {
            price = Float.valueOf(priceString);
            if (price < 0) {
                DBLogger.logRejectedRecord("OFFER", "price", "Price is negative: " + price + " for product: " + newProduct.getProductId());
                return false;
            }
        }

        Offer newOffer = new Offer(price, state, newShop.getShopId(), newProduct.getProductId());
        newOffer.createOffer(con);
        return true;
    }
}