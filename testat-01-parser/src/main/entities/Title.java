package main.entities;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Title class representing the Title entity and its corresponding attributes
public class Title {
    private int titleId;
    private String name;
    private int cdId;

    // Method to create a title in the database
    public void createTitle(Connection conn) {
        var sql = "INSERT INTO Title (name, cd_id) Values(?, ?) RETURNING title_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.name);
            pstmt.setInt(2, this.cdId);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.titleId = rs.getInt("title_id");
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("TITLE", "-", exception.getMessage());
        }
    }

    // Method to check if a title with the same name and cd_id already exists in the database
    public boolean checkForTitleName(Connection conn) {

        var sql = "SELECT * FROM Title WHERE name = ? AND cd_id = ?";
        try {
            var pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, this.name);
            pstmt.setInt(1, this.cdId);
            var rs = pstmt.executeQuery();

            return rs.isBeforeFirst();

        } catch (SQLException e) {
            DBLogger.logRejectedRecord("TITLE", "-", e.getMessage());
        }

        return false;
    }


    public Title(String name, int cdId) {
        this.name = name;
        this.cdId = cdId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Title{" +
                "titleId=" + titleId +
                ", name='" + name + '\'' +
                ", cdId=" + cdId +
                '}';
    }
}