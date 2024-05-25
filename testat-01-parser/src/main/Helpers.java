/**
 * The Helpers class provides utility methods for environment variable loading and XML document parsing.
 * It includes methods to retrieve environment variables, find nodes in XML documents by tag name, get inner text of a node,
 * and get attribute values from a node.
 * <p>
 * Methods:
 * - loadEnv(String env): Loads and returns the value of the specified environment variable.
 * - getFirstNodeByTagName(Element itemElement, String name): Returns the first node with the specified tag name from the given Element.
 * - getFirstNodeByTagName(Document itemElement, String name): Returns the first node with the specified tag name from the given Document.
 * - getInnerText(Node): Returns the text content of the specified node.
 * - getAttributeValue(Node, String attributeName): Returns the value of the specified attribute from the given node, or null if the attribute is not found.
 */

package main;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Helpers {

    public static String loadEnv(String env) {
        return System.getenv(env);
    }

    public static Node getFirstNodeByTagName(Element itemElement, String name) {
        return itemElement.getElementsByTagName(name).item(0);
    }

    public static Node getFirstNodeByTagName(Document itemElement, String name) {
        return itemElement.getElementsByTagName(name).item(0);
    }

    public static String getInnerText(Node node) {
        return node.getTextContent();
    }

    public static String getAttributeValue(Node node, String attributeName) {
        NamedNodeMap nodeAttributes = node.getAttributes();

        var attribute = nodeAttributes.getNamedItem(attributeName);

        if (attribute == null) {
            return null;
        }

        return attribute.getNodeValue().trim();
    }

}
