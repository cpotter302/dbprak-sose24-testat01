package main.entities;

import main.Helpers;
import main.logger.DBLogger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Person class representing the Person entity and its corresponding attributes
public class Person {
    private int personId;
    private String name;

    public Person( String name) {
        this.name = name;
    }

    // Method to create a person record in the database
    public void createPerson(Connection conn){
        var sql = "INSERT INTO Person (name) Values(?) RETURNING person_id";

        try {
            var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, this.getName());

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            this.personId = rs.getInt("person_id");
            DBLogger.LOG.info(this.toString());
        } catch (SQLException exception) {
            DBLogger.logRejectedRecord("PERSON", "-", exception.getMessage());
        }
    }

    // Method to get the person ID from the database
    public int getPersonID(Connection conn){
        var sql = "SELECT * FROM Person WHERE name = ?";
        try {
            var pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, this.name);
            var rs = pstmt.executeQuery();

            rs.next();

            return rs.getInt("person_id");

        } catch (SQLException e) {
            DBLogger.logRejectedRecord("PERSON", "?", e.getMessage());
            return -1;
        }
    }

    // Method to check if a person ID exists in the database
    public boolean checkForPersonID(Connection conn) {

        var sql = "SELECT * FROM Person WHERE name = ?";
        try {
            var pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, this.name);
            var rs = pstmt.executeQuery();

            return rs.isBeforeFirst();

        } catch (SQLException e) {
            DBLogger.logRejectedRecord("PERSON", "-", e.getMessage());
        }

        return false;
    }

    // Method to create a person object and connect it to a DVD or CD object
    public static void createObjectPersons(Connection conn, String personType, Element itemElement, DVD newDVD, CD newCD){
        var personList = itemElement.getElementsByTagName(personType);
        var personListLength = personList.getLength();

        Node currentPerson;
        for (int j = 0; j < personListLength; j++) {
            currentPerson = personList.item(j);
            if (currentPerson.getNodeType() == Node.ELEMENT_NODE) {
                var personTextContent = currentPerson.getTextContent();
                var personAttributeValue = Helpers.getAttributeValue(currentPerson, "name");
                String author;
                if (personTextContent.isEmpty() && (personAttributeValue == null || personAttributeValue.isEmpty())) {
                    DBLogger.logRejectedRecord("PERSON", "-", "Person has no name");
                } else {
                    if (personTextContent.isEmpty()) {
                        author = personAttributeValue;
                    } else {
                        author = personTextContent;
                    }
                    Person person = new Person(author);
                    if (!person.checkForPersonID(conn)) {
                        person.createPerson(conn);
                    }else{
                        person.setPersonId(person.getPersonID(conn));
                    }

                    if(newDVD != null) {
                        // Create DVD Person Connection
                        ObjectPerson dvdPerson = new ObjectPerson(newDVD.getDvdId(), person.getPersonId(), personType);
                        dvdPerson.createDVDPerson(conn);
                    } else {
                        // Create CD Person Connection
                        ObjectPerson cdPerson = new ObjectPerson(newCD.getCdId(), person.getPersonId(), personType);
                        cdPerson.createCDPerson(conn);
                    }

                }
            }
        }
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "personId=" + personId +
                ", name='" + name + '\'' +
                '}';
    }
}

