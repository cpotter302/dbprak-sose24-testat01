package main.entities;

import main.logger.DBLogger;

import java.sql.*;

// CD class representing the CD entity and its corresponding attributes
public class CD {
    private int cdId;
    private String productId;
    private String label;
    private Date publishDate;

    // Method to create a CD record in the database
    public void createCD(Connection conn) {
        var sql = "INSERT INTO CD (label, publishdate, product_id) Values(?, ?, ?) RETURNING cd_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.label);
            pstmt.setDate(2, this.publishDate);
            pstmt.setString(3, this.productId);


            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.cdId = rs.getInt("cd_id");

            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("CD", "-", exception.getMessage());
        }
    }

    public int getCdId() {
        return cdId;
    }

    public void setCdId(int cdId) {
        this.cdId = cdId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public CD(String productId, String label, Date publishDate) {
        this.productId = productId;
        this.label = label;
        this.publishDate = publishDate;
    }

    @Override
    public String toString() {
        return "CD{" +
                "cdId=" + cdId +
                ", productId='" + productId + '\'' +
                ", label='" + label + '\'' +
                ", publishDate=" + publishDate +
                '}';
    }
}