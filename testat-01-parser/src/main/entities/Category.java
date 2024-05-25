package main.entities;

import main.logger.DBLogger;

import java.sql.*;

// Category class representing the Category entity and its corresponding attributes
public class Category {
    private int categoryId;
    private String name;
    private Integer parentCategory;

    public Category(String name, Integer parentCategory) {
        this.name = name;
        this.parentCategory = parentCategory;
    }

    // Method to create a category record in the database
    public void createCategory(Connection conn) {
        var sql = "INSERT INTO Category (name, parent_category) Values(?, ?) RETURNING category_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.name);
            if (this.parentCategory != null) {
                pstmt.setInt(2, this.parentCategory);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.categoryId = rs.getInt("category_id");
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("CATEGORY", "-", exception.getMessage());
        }
    }

    // Method to check if a category with the same name and parent category already exists in the database
    public boolean checkForCategoryNameAndParent(Connection conn) {

        var sql = "SELECT * FROM Category WHERE name = ? AND parent_category = ?";
        try {
            var pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, this.name);
            if (this.parentCategory != null) {
                pstmt.setInt(2, this.parentCategory);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            var rs = pstmt.executeQuery();

            return rs.isBeforeFirst();

        } catch (SQLException e) {
            DBLogger.logRejectedRecord("CATEGORY", "-", e.getMessage());
        }

        return false;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Integer parentCategory) {
        this.parentCategory = parentCategory;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", parentCategory=" + parentCategory +
                '}';
    }
}
