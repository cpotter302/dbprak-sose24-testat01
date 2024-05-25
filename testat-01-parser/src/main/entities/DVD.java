package main.entities;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// DVD class representing the DVD entity and its corresponding attributes
public class DVD {
    private int dvdId;
    private String format;
    private int runningTime;
    private String regionCode;
    private String productId;
    // getters and setters

    // Method to create a DVD record in the database
    public void createDVD(Connection conn) {
        var sql = "INSERT INTO DVD (format, runningtime, region_code, product_id) Values(?, ?, ?, ?) RETURNING dvd_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, this.format);
            pstmt.setInt(2, this.runningTime);
            pstmt.setString(3, this.regionCode);
            pstmt.setString(4, this.productId);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.dvdId = rs.getInt("dvd_id");
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("DVD", "-", exception.getMessage());
        }
    }

    public int getDvdId() {
        return dvdId;
    }

    public DVD(String format, int runningTime, String regionCode, String productId) {
        this.format = format;
        this.runningTime = runningTime;
        this.regionCode = regionCode;
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "DVD{" +
                "dvdId=" + dvdId +
                ", format=" + format +
                ", laufzeit=" + runningTime +
                ", regionCode='" + regionCode + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}