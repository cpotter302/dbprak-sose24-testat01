package main.entities;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Shop class representing the Shop entity and its corresponding attributes
public class Shop {
    private int shopId;
    private String name;
    private String address;

    // Method to create a shop in the database
    public void createShop(Connection conn) {

        var sql_newShop = "INSERT INTO Shop (name, address) Values(?, ?) RETURNING shop_id";

        try {
            var pstmt = conn.prepareStatement(sql_newShop, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.name);
            pstmt.setString(2, this.address);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.shopId = rs.getInt("shop_id");
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("SHOP", "-", exception.getMessage());
        }
    }

    public Shop(String street, String zip, String city) {
        this.name = city;
        this.address = String.format("%s, %s, %s", street, zip, city);
    }

    public int getShopId() {
        return shopId;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "shopId=" + shopId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
