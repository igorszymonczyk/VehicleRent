CREATE DATABASE IF NOT EXISTS car_rental;
USE car_rental;

CREATE TABLE cars (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    licensePlate VARCHAR(20) NOT NULL UNIQUE, -- Dodanie unikalnego numeru rejestracyjnego
    numberOfDoors INT NOT NULL,               -- Liczba drzwi
    seats INT NOT NULL,                        -- Liczba miejsc
    isAvailable BOOLEAN DEFAULT TRUE           -- Status dostępności
);

CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20)
);

CREATE TABLE rentals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    car_id INT,
    customer_id INT,
    rental_date DATE NOT NULL,
    return_date DATE,
    FOREIGN KEY (car_id) REFERENCES cars(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

INSERT INTO cars (brand, model, licensePlate, numberOfDoors, seats, isAvailable) VALUES
('Toyota', 'Corolla', 'ABC123', 4, 5, TRUE),
('Honda', 'Civic', 'XYZ456', 4, 5, TRUE),
('Ford', 'Mustang', 'MST789', 2, 4, TRUE);

INSERT INTO customers (first_name, last_name, email, phone_number) VALUES
('Jan', 'Kowalski', 'jan.kowalski@example.com', '123456789'),
('Anna', 'Nowak', 'anna.nowak@example.com', '987654321');

INSERT INTO rentals (car_id, customer_id, rental_date, return_date) VALUES
(1, 1, '2023-09-01', '2023-09-10'),
(2, 2, '2023-09-05', NULL); -- Samochód wciąż wypożyczony
