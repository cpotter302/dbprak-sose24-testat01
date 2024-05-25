package main.entities;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// ObjectPerson class representing the jucntion N:M entities for DVD, CD, BOOK and its corresponding attributes
public class ObjectPerson {
    private int objectId;
    private int personId;
    private String role;

    // Method to create a book_person record in the database
    public void createBookPerson(Connection conn) {
        var sql = "INSERT INTO Book_Person (book_id, person_id) Values(?, ?)";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, this.objectId);
            pstmt.setInt(2, this.personId);

            pstmt.executeUpdate();
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("BOOK_PERSON", "-", exception.getMessage());
        }
    }

    // Method to create a cd_person record in the database
    public void createCDPerson(Connection conn) {
        var sql = "INSERT INTO CD_Person (cd_id, person_id, role) Values(?, ?, ?)";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, this.objectId);
            pstmt.setInt(2, this.personId);
            pstmt.setString(3, this.role);

            pstmt.executeUpdate();
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("CD_PERSON", "-", exception.getMessage());
        }
    }

    // Method to create a dvd_person record in the database
    public void createDVDPerson(Connection conn) {
        var sql = "INSERT INTO DVD_Person (dvd_id, person_id, role) Values(?, ?, ?)";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, this.objectId);
            pstmt.setInt(2, this.personId);
            pstmt.setString(3, this.role);

            pstmt.executeUpdate();
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("DVD_PERSON", "-", exception.getMessage());
        }
    }

    public ObjectPerson(int objectId, int personId) {
        this.objectId = objectId;
        this.personId = personId;
    }

    public ObjectPerson(int objectId, int personId, String role) {
        this.objectId = objectId;
        this.personId = personId;
        this.role = role;
    }

    @Override
    public String toString() {
        return "ObjectPerson{" +
                "bookId=" + objectId +
                ", personId=" + personId +
                ", role=" + role +
                '}';
    }
}
