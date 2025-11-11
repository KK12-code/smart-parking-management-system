package com.parking;

// made a separate class in case we later want special rates for trucks
public class Truck extends Vehicle {

    public Truck(String licensePlate) {
        super(licensePlate, "Truck");
    }
}
