package main.entities;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Customer class representing the Customer entity and its corresponding attributes
public class Customer {
    private final String customerId;
    private String bankAccountNumber;
    private String address;

    public Customer(String customerId) {
        this.customerId = customerId;
    }

    // Method to create a customer record in the database
    public void createCustomer(Connection conn){
        var sql = "INSERT INTO Customer (customer_id) Values(?)";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.customerId);

            pstmt.executeUpdate();
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("CUSTOMER", "-", exception.getMessage());
        }
    }

    // Method to check if a customer with the same customer id already exists in the database
    public boolean checkForCustomerID(Connection conn) {

        var sql = "SELECT * FROM Customer WHERE customer_id = ?";
        try {
            var pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, this.customerId);
            var rs = pstmt.executeQuery();

            return rs.isBeforeFirst();

        } catch (SQLException e) {
            DBLogger.logRejectedRecord("CUSTOMER", "-", e.getMessage());
        }

        return false;
    }

    public String getCustomerId() {
        return customerId;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", bankAccountNumber='" + bankAccountNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
