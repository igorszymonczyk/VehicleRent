package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Rental {
    private int id;
    private Car car;
    private int customerId;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private String renterName;
    private String email;

    public Rental(int id, Car car, int customerId, LocalDate rentalDate, LocalDate returnDate, String renterName, String email) {
        this.id = id;
        this.car = car;
        this.customerId = customerId;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.renterName = renterName;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public void rentCar(Database database) {
        String rentCarQuery = "INSERT INTO rentals (car_id, customer_id, rental_date, return_date) VALUES (?, ?, ?, NULL)";
        String updateCarQuery = "UPDATE cars SET isAvailable = false WHERE id = ?";
        String addCustomerQuery = "INSERT INTO customers (first_name, last_name, email) VALUES (?, ?, ?)";
        String checkCustomerQuery = "SELECT id FROM customers WHERE email = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement rentCarStmt = connection.prepareStatement(rentCarQuery);
             PreparedStatement updateCarStmt = connection.prepareStatement(updateCarQuery);
             PreparedStatement addCustomerStmt = connection.prepareStatement(addCustomerQuery);
             PreparedStatement checkCustomerStmt = connection.prepareStatement(checkCustomerQuery)) {

            String[] nameParts = this.renterName.split(" ", 2);
            String firstName = nameParts.length > 0 ? nameParts[0] : "";
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            checkCustomerStmt.setString(1, email);
            ResultSet rs = checkCustomerStmt.executeQuery();

            if (rs.next()) {
                customerId = rs.getInt("id");
            } else {
                addCustomerStmt.setString(1, firstName);
                addCustomerStmt.setString(2, lastName);
                addCustomerStmt.setString(3, email);
                addCustomerStmt.executeUpdate();
                customerId = getLastInsertedCustomerId(connection);
            }

            rentCarStmt.setInt(1, car.getId());
            rentCarStmt.setInt(2, customerId);
            rentCarStmt.setDate(3, java.sql.Date.valueOf(rentalDate));
            rentCarStmt.executeUpdate();

            updateCarStmt.setInt(1, car.getId());
            updateCarStmt.executeUpdate();

            car.setAvailable(false);
            System.out.println("Car rented successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private int getLastInsertedCustomerId(Connection connection) throws SQLException {
        String query = "SELECT LAST_INSERT_ID()";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to retrieve last inserted customer ID.");
        }
    }

    public void returnCar(Database database) {
        String updateRentalQuery = "UPDATE rentals SET return_date = ? WHERE car_id = ? AND customer_id = ?";
        String updateCarQuery = "UPDATE cars SET isAvailable = true WHERE id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement updateRentalStmt = connection.prepareStatement(updateRentalQuery);
             PreparedStatement updateCarStmt = connection.prepareStatement(updateCarQuery)) {

            updateRentalStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            updateRentalStmt.setInt(2, car.getId());
            updateRentalStmt.setInt(3, customerId);
            int rowsUpdated = updateRentalStmt.executeUpdate();

            if (rowsUpdated > 0) {
                updateCarStmt.setInt(1, car.getId());
                updateCarStmt.executeUpdate();
                car.setAvailable(true);
                System.out.println("Car returned successfully!");
            } else {
                System.out.println("This car has already been returned or does not exist in the rental records.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
