package com.parking;

// tiny helper class so we can add bike-specific behavior later without touching Vehicle
public class Bike extends Vehicle {

    public Bike(String licensePlate) {
        super(licensePlate, "Bike");
    }
}
