package models;

import java.util.Scanner;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/car_rental";
        String user = "root";
        String password = "password";

        Database database = new Database(url, user, password);
        CarRentalSystem rentalSystem = new CarRentalSystem(database);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Select user type:");
            System.out.println("1. Customer");
            System.out.println("2. Admin");
            System.out.println("3. Exit");

            int userType = getUserInput(scanner);

            if (userType == 3) {
                System.out.println("Exiting...");
                break;
            }

            if (userType == 2) {
                if (!authenticateAdmin(scanner)) {
                    continue;
                }
                handleAdminOptions(scanner, rentalSystem);
            } else if (userType == 1) {
                handleCustomerOptions(scanner, rentalSystem);
            } else {
                System.out.println("Invalid user type selected.");
            }
        }

        scanner.close();
    }

    private static int getUserInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static boolean authenticateAdmin(Scanner scanner) {
        System.out.print("Enter admin password: ");
        String passwordInput = scanner.nextLine();
        if (!passwordInput.equals("admin")) {
            System.out.println("Incorrect password.");
            return false;
        }
        return true;
    }

    private static void handleAdminOptions(Scanner scanner, CarRentalSystem rentalSystem) {
        while (true) {
            System.out.println("Admin Options:");
            System.out.println("1. Add a car");
            System.out.println("2. Remove a car");
            System.out.println("3. Check all active rentals");
            System.out.println("4. Back to user selection");

            int adminChoice = getUserInput(scanner);

            switch (adminChoice) {
                case 1:
                    addCar(scanner, rentalSystem);
                    break;
                case 2:
                    removeCar(scanner, rentalSystem);
                    break;
                case 3:
                    rentalSystem.displayActiveRentals();
                    break;
                case 4:
                    System.out.println("Returning to user selection...");
                    return;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    private static void addCar(Scanner scanner, CarRentalSystem rentalSystem) {
        System.out.println("Enter car details:");
        System.out.print("Brand: ");
        String brand = scanner.nextLine();
        System.out.print("Model: ");
        String model = scanner.nextLine();
        System.out.print("License Plate: ");
        String licensePlate = scanner.nextLine();

        int numberOfDoors = getPositiveInteger(scanner, "Number of Doors: ");
        int seats = getPositiveInteger(scanner, "Seats: ");

        Car newCar = new Car(0, brand, model, licensePlate, numberOfDoors, seats);
        rentalSystem.addCar(newCar);
        System.out.println("Car added successfully!");
    }

    private static void removeCar(Scanner scanner, CarRentalSystem rentalSystem) {
        rentalSystem.displayAllCars();
        System.out.println("Enter the ID of the car to remove:");
        int carId = getUserInput(scanner);
        rentalSystem.removeCarById(carId);
    }

    private static void handleCustomerOptions(Scanner scanner, CarRentalSystem rentalSystem) {
        while (true) {
            System.out.println("Customer Options:");
            System.out.println("1. Rent a car");
            System.out.println("2. Return a car");
            System.out.println("3. Back to user selection");

            int choice = getUserInput(scanner);

            switch (choice) {
                case 1:
                    rentCar(scanner, rentalSystem);
                    break;
                case 2:
                    returnCar(scanner, rentalSystem);
                    break;
                case 3:
                    System.out.println("Returning to user selection...");
                    return;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    private static void rentCar(Scanner scanner, CarRentalSystem rentalSystem) {
        System.out.println("Available cars:");
        rentalSystem.displayAvailableCars();
        System.out.print("Enter the ID of the car you want to rent: ");
        int carIdToRent = getUserInput(scanner);

        System.out.print("Enter your name: ");
        String customerName = scanner.nextLine();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        LocalDate rentalDate = LocalDate.now();
        System.out.print("Enter the number of days you want to rent the car: ");
        int rentalDays = getUserInput(scanner);
        LocalDate returnDate = rentalDate.plusDays(rentalDays);

        rentalSystem.rentCarById(carIdToRent, customerName, email, rentalDate, returnDate);
        System.out.println("Car rented successfully!");
    }

    private static void returnCar(Scanner scanner, CarRentalSystem rentalSystem) {
        System.out.print("Enter the license plate of the car you want to return: ");
        String licensePlateToReturn = scanner.nextLine();
        rentalSystem.returnCar(licensePlateToReturn);
        System.out.println("Car returned successfully!");
    }

    private static int getPositiveInteger(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value > 0) {
                    return value;
                } else {
                    System.out.println("Please enter a number greater than zero.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
