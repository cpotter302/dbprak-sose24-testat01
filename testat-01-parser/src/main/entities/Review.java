package main.entities;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Review class representing the Review entity and its corresponding attributes
public class Review {
    private int reviewId;
    private Customer customer;
    private int rating;
    private String productId;
    private String description;

    // Method to create a review in the database
    public void createReview(Connection conn) {
        var sql = "INSERT INTO Review (customer_id, rating, product_id, description) Values(?, ?, ?, ?) RETURNING review_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.customer.getCustomerId());
            pstmt.setInt(2, this.rating);
            pstmt.setString(3, this.productId);
            pstmt.setString(4, this.description);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.reviewId = rs.getInt("review_id");
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("REVIEW", "-", exception.getMessage());
        }
    }

    public Review(Customer customer, int rating, String productId) {
        this.customer = customer;
        this.rating = rating;
        this.productId = productId;
    }

    public void setDescription(String description) {
        String regex = "<[^>]*>|&[^;]+;";
        this.description = description.replaceAll("\"", "").replaceAll(regex, "");
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", customer=" + customer.toString() +
                ", rating=" + rating +
                ", productId='" + productId + '\'' +
                '}';
    }
}