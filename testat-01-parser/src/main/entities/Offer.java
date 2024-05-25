package main.entities;

import main.logger.DBLogger;

import java.sql.*;

// Offer class representing the Offer entity and its corresponding attributes
public class Offer {
    private int offerId;
    private Float price;
    private String condition;
    private int shopId;
    private String productId;

    public Offer(Float price, String condition, int shopId, String productId) {
        this.price = price;
        this.condition = condition;
        this.shopId = shopId;
        this.productId = productId;
    }

    // Method to create an offer record in the database
    public void createOffer(Connection conn) {

        var sql = "INSERT INTO Offer (price, condition, shop_id, product_id) Values(?, ?, ?, ?) RETURNING offer_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            if (this.price != null) {
                pstmt.setFloat(1, this.price);
            } else {
                pstmt.setNull(1, Types.FLOAT);
            }
            pstmt.setString(2, this.condition);
            pstmt.setInt(3, this.shopId);
            pstmt.setString(4, this.productId);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.offerId = rs.getInt("offer_id");
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("OFFER", "-", exception.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Offer{" +
                "offerId=" + offerId +
                ", price=" + price +
                ", condition='" + condition + '\'' +
                ", shopId=" + shopId +
                ", productId='" + productId + '\'' +
                '}';
    }

}