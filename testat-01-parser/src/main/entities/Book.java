package main.entities;

import main.logger.DBLogger;

import java.sql.*;

// Book class representing the Book entity and its corresponding attributes
public class Book {
    private int bookId;
    private int pageAmount;
    private Date publishDate;
    private String isbn;
    private String publisher;
    private String productId;

    // Method to create a book record in the database
    public void createBook(Connection conn) {
        var sql = "INSERT INTO Book (pageamount, publishdate, isbn, publisher, product_id) Values(?, ?, ?, ?, ?) RETURNING book_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, this.pageAmount);
            pstmt.setDate(2, this.publishDate);
            pstmt.setString(3, this.isbn);
            pstmt.setString(4, this.publisher);
            pstmt.setString(5, this.productId);


            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.bookId = rs.getInt("book_id");

            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("BOOK", "-", exception.getMessage());
        }
    }

    public int getBookId() {
        return bookId;
    }

    public Book(int pageAmount, Date publishDate, String isbn, String publisher, String productId) {
        this.pageAmount = pageAmount;
        this.publishDate = publishDate;
        this.isbn = isbn;
        this.publisher = publisher;
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", pageAmount=" + pageAmount +
                ", publishDate=" + publishDate +
                ", isbn='" + isbn + '\'' +
                ", publisher='" + publisher + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}