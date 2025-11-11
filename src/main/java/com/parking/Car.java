package com.parking;

// nothing fancy here yet — just tagging the vehicle type so ParkingLot can print something friendly
public class Car extends Vehicle {

    public Car(String licensePlate) {
        super(licensePlate, "Car");
    }
}
