package main.entities;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import main.logger.DBLogger;

// Product class representing the Product entity and its corresponding attributes
public class Product {
    private String productId;
    private String title;
    private int salesrank;
    private String image;
    private String productGroup;
    private String ean;
    private Float averageRating;

    // Method to create a product record in the database
    public void createProduct(Connection conn) {
        var sql = "INSERT INTO Product (product_id, title, salesrank, image, pgroup) Values(?, ?, ?, ?, ?)";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.productId);
            pstmt.setString(2, this.title);
            pstmt.setInt(3, this.salesrank);
            pstmt.setString(4, this.image);
            pstmt.setString(5, this.productGroup);

            pstmt.executeUpdate();
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("PRODUCT", "-", exception.getMessage());
        }
    }

    // Method to get the product ID from the database
    public boolean checkForProductID(Connection conn) {

        var sql = "SELECT * FROM Product WHERE product_id = ?";
        try {
            var pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, this.getProductId());
            var rs = pstmt.executeQuery();

            return rs.isBeforeFirst();

        } catch (SQLException e) {
            DBLogger.logRejectedRecord("PRODUCT", "-", e.getMessage());
        }

        return false;
    }

    // Method to calculate the average ratings of the products based on the reviews
    // The method calculate_average_ratings() is a stored procedure in the database
    public static void calculateAverageRatings(Connection conn) {
        var sql = "SELECT calculate_average_ratings()";

        try {
            conn.prepareStatement(sql).executeQuery();
            DBLogger.LOG.info(sql);
            DBLogger.LOG.info("Average ratings calculated");
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("REVIEW", "-", exception.getMessage());
        }
    }


    public String getProductId() {
        return productId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSalesrank(int salesrank) {
        this.salesrank = salesrank;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public Product(String productId, String title, int salesrank, String image, String productGroup) {
        this.productId = productId;
        this.title = title;
        this.salesrank = salesrank;
        this.image = image;
        this.productGroup = productGroup;
    }

    public Product(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", title='" + title + '\'' +
                ", salesrank=" + salesrank +
                ", ean='" + ean + '\'' +
                ", averageRating=" + averageRating +
                '}';
    }
}