package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class CarRentalSystem {
    private Database database;

    public CarRentalSystem(Database database) {
        this.database = database;
    }

    private int getCustomerId(String email) {
        String query = "SELECT id FROM customers WHERE email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void rentCarById(int carId, String renterName, String email, LocalDate startDate, LocalDate endDate) {
        Car car = getCarById(carId);
        if (car != null && car.isAvailable()) {
            int customerId = getCustomerId(email);
            if (customerId == -1) {
                registerCustomer(renterName, email);
                customerId = getCustomerId(email);
                if (customerId == -1) {
                    System.out.println("Failed to register customer. Cannot proceed with rental.");
                    return;
                }
            }

            if (endDate == null) {
                endDate = startDate.plusDays(7);
            }

            String query = "INSERT INTO rentals (car_id, customer_id, rental_date, return_date) VALUES (?, ?, ?, NULL)";
            try (Connection connection = database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, carId);
                statement.setInt(2, customerId);
                statement.setDate(3, Date.valueOf(startDate));
                statement.executeUpdate();
                System.out.println("Car with ID " + carId + " has been rented.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Car with ID " + carId + " is not available or not found.");
        }
    }

    public void removeCarById(int carId) {
        Car car = getCarById(carId);
        if (car != null) {
            String deleteQuery = "DELETE FROM cars WHERE id = ?";
            try (Connection connection = database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(deleteQuery)) {

                statement.setInt(1, carId);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Car with ID " + carId + " has been removed from the database.");
                } else {
                    System.out.println("Failed to remove the car.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Car with ID " + carId + " not found.");
        }
    }

    public void displayAllCars() {
        String query = "SELECT * FROM cars";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("All cars:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String brand = resultSet.getString("brand");
                String model = resultSet.getString("model");
                String licensePlate = resultSet.getString("licensePlate");
                int numberOfDoors = resultSet.getInt("numberOfDoors");
                int seats = resultSet.getInt("seats");

                System.out.println("ID: " + id + ", Brand: " + brand + ", Model: " + model +
                        ", License Plate: " + licensePlate + ", Doors: " + numberOfDoors + ", Seats: " + seats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerCustomer(String customerName, String email) {
        String query = "INSERT INTO customers (first_name, last_name, email) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            String[] nameParts = customerName.split(" ", 2); // Podziel imię i nazwisko
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : ""; // Jeśli nie ma nazwiska, ustaw jako pusty string

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.executeUpdate();

            System.out.println("Customer registered successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCar(Car car) {
        if (isLicensePlateExists(car.getLicensePlate())) {
            System.out.println("Car with this license plate already exists in the database.");
            return;
        }

        String query = "INSERT INTO cars (brand, model, licensePlate, numberOfDoors, seats, isAvailable) VALUES (?, ?, ?, ?, ?, true)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, car.getBrand());
            statement.setString(2, car.getModel());
            statement.setString(3, car.getLicensePlate());
            statement.setInt(4, car.getNumberOfDoors());
            statement.setInt(5, car.getSeats());
            statement.executeUpdate();

            System.out.println("Car added to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isLicensePlateExists(String licensePlate) {
        String query = "SELECT COUNT(*) FROM cars WHERE licensePlate = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, licensePlate);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean returnCar(String licensePlate) {
        String query = "UPDATE rentals SET return_date = ? " +
                "WHERE car_id = (SELECT id FROM cars WHERE licensePlate = ?) " +
                "AND return_date IS NULL";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(LocalDate.now()));
            statement.setString(2, licensePlate);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                updateCarAvailability(licensePlate, true);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateCarAvailability(String licensePlate, boolean isAvailable) {
        String query = "UPDATE cars SET isAvailable = ? WHERE licensePlate = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, isAvailable);
            statement.setString(2, licensePlate);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Rental findRentalByLicensePlate(String licensePlate) {
        String query = "SELECT rentals.id AS rental_id, " +
                "cars.id AS car_id, cars.brand, cars.model, cars.licensePlate, cars.numberOfDoors, cars.seats, " +
                "rentals.customer_id, rentals.rental_date, rentals.return_date, " +
                "customers.first_name, customers.last_name, customers.email " +
                "FROM rentals " +
                "JOIN cars ON rentals.car_id = cars.id " +
                "JOIN customers ON rentals.customer_id = customers.id " +
                "WHERE cars.licensePlate = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, licensePlate);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("rental_id");
                Car car = new Car(
                        rs.getInt("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("licensePlate"),
                        rs.getInt("numberOfDoors"),
                        rs.getInt("seats")
                );

                int customerId = rs.getInt("customer_id");
                LocalDate rentalDate = rs.getDate("rental_date").toLocalDate();
                LocalDate returnDate = rs.getDate("return_date") != null
                        ? rs.getDate("return_date").toLocalDate()
                        : null;

                String renterName = rs.getString("first_name") + " " + rs.getString("last_name");
                String email = rs.getString("email");

                return new Rental(id, car, customerId, rentalDate, returnDate, renterName, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void displayActiveRentals() {
        String query = """
        SELECT r.car_id, c.brand, c.model, cu.first_name, cu.last_name, cu.email, r.rental_date, r.return_date
        FROM rentals r
        JOIN cars c ON r.car_id = c.id
        JOIN customers cu ON r.customer_id = cu.id
        WHERE r.return_date IS NULL
    """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Active Rentals:");
            while (resultSet.next()) {
                int carId = resultSet.getInt("car_id");
                String brand = resultSet.getString("brand");
                String model = resultSet.getString("model");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                LocalDate startDate = resultSet.getDate("rental_date").toLocalDate();

                System.out.println("Car ID: " + carId + ", Brand: " + brand + ", Model: " + model +
                        ", Renter: " + firstName + " " + lastName + ", Email: " + email +
                        ", Start Date: " + startDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayAvailableCars() {
        String query = "SELECT * FROM cars WHERE isAvailable = true";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Available cars:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String brand = resultSet.getString("brand");
                String model = resultSet.getString("model");
                String licensePlate = resultSet.getString("licensePlate");
                int numberOfDoors = resultSet.getInt("numberOfDoors");
                int seats = resultSet.getInt("seats");

                System.out.println("ID: " + id + ", Brand: " + brand + ", Model: " + model +
                        ", License Plate: " + licensePlate + ", Doors: " + numberOfDoors + ", Seats: " + seats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Car getCarByLicensePlate(String licensePlate) {
        String query = "SELECT * FROM cars WHERE licensePlate = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, licensePlate);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Car(
                        resultSet.getInt("id"),
                        resultSet.getString("brand"),
                        resultSet.getString("model"),
                        resultSet.getString("licensePlate"),
                        resultSet.getInt("numberOfDoors"),
                        resultSet.getInt("seats")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Car> getAvailableCars() {
        String query = "SELECT * FROM cars WHERE isAvailable = TRUE";
        List<Car> availableCars = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Car car = new Car(
                        rs.getInt("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("licensePlate"),
                        rs.getInt("numberOfDoors"),
                        rs.getInt("seats")
                );
                availableCars.add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableCars;
    }

    public List<Rental> getActiveRentals() {
        List<Rental> rentals = new ArrayList<>();
        String query = "SELECT rentals.id AS rental_id, " +
                "cars.id AS car_id, cars.brand, cars.model, cars.licensePlate, " +
                "customers.first_name, customers.last_name, customers.email, " +
                "rentals.rental_date " +
                "FROM rentals " +
                "JOIN cars ON rentals.car_id = cars.id " +
                "JOIN customers ON rentals.customer_id = customers.id " +
                "WHERE rentals.return_date IS NULL";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                Car car = new Car(
                        rs.getInt("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("licensePlate"),
                        rs.getInt("numberOfDoors"),
                        rs.getInt("seats")
                );

                String renterName = rs.getString("first_name") + " " + rs.getString("last_name");
                String email = rs.getString("email");
                LocalDate rentalDate = rs.getDate("rental_date").toLocalDate();

                rentals.add(new Rental(rentalId, car, rs.getInt("customer_id"), rentalDate, null, renterName, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentals;
    }

    private Car getCarById(int id) {
        String query = "SELECT * FROM cars WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Car(
                        resultSet.getInt("id"),
                        resultSet.getString("brand"),
                        resultSet.getString("model"),
                        resultSet.getString("licensePlate"),
                        resultSet.getInt("numberOfDoors"),
                        resultSet.getInt("seats")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
