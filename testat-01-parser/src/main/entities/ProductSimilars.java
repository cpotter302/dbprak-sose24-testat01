package main.entities;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// ProductSimilars class representing the Product_Similars entity and its corresponding attributes
public class ProductSimilars {
    private String productId;
    private String similarProduct;

    public ProductSimilars(String productId, String similarProduct) {
        this.productId = productId;
        this.similarProduct = similarProduct;
    }

    // Method to create a product similar record in the database
    public void createProductSimilar(Connection conn) {
        var sql = "INSERT INTO Product_Similars (product_id, similar_product) Values(?, ?)";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.productId);
            pstmt.setString(2, this.similarProduct);

            pstmt.executeUpdate();
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("PRODUCT_SIMILARS", "-", exception.getMessage());
        }
    }

    // Method to check if a product similar exists in the database
    public boolean checkForProductSimilar(Connection conn) {
        var sql = "SELECT * FROM Product WHERE product_id = ?";
        try {
            var pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, this.similarProduct);
            var rs = pstmt.executeQuery();

            return rs.isBeforeFirst();
        } catch (SQLException e) {
            DBLogger.logRejectedRecord("PRODUCT_SIMILARS", "-", e.getMessage());
        }

        return false;
    }

    @Override
    public String toString() {
        return "ProductSimilars{" +
                "productId='" + productId + '\'' +
                ", similarProduct='" + similarProduct + '\'' +
                '}';
    }
}