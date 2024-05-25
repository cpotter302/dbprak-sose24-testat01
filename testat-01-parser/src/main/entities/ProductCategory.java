package main.entities;

import main.logger.DBLogger;

import java.sql.*;

// ProductCategory class representing the Product_Category entity and its corresponding attributes
public class ProductCategory {

    private String productId;
    private int categoryId;

    public ProductCategory(String productId, int categoryId) {
        this.productId = productId;
        this.categoryId = categoryId;
    }

    // Method to create a product category record in the database
    public void createCategory(Connection conn) {
        var sql = "INSERT INTO Product_Category (product_id, category_id) Values(?, ?)";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.productId);
            pstmt.setInt(2, this.categoryId);

            pstmt.executeUpdate();
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("PRODUCT_CATEGORY", "-", exception.getMessage());
        }
    }

    @Override
    public String toString() {
        return "ProductCategory{" +
                "productId='" + productId + '\'' +
                ", categoryId=" + categoryId +
                '}';
    }
}
