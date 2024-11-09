package models;

public class Car extends Vehicle {
    private int id;
    private int numberOfDoors;
    private int seats;

    public Car(int id, String brand, String model, String licensePlate, int numberOfDoors, int seats) {
        super(brand, model, licensePlate);
        this.id = id;
        this.numberOfDoors = numberOfDoors;
        this.seats = seats;
        setAvailable(true);
    }

    public int getId() {
        return id;
    }

    public int getNumberOfDoors() {
        return numberOfDoors;
    }

    public int getSeats() {
        return seats;
    }
}
