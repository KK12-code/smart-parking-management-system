package com.parking;

/**
 * Abstract representation of a vehicle in the parking system.
 * Every specific vehicle type simply describes its own type string.
 */
public abstract class Vehicle {
    private final String licensePlate;
    private final String vehicleType;

    protected Vehicle(String licensePlate, String vehicleType) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be empty.");
        }
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }
}
