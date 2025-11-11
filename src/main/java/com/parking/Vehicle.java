package com.parking;

// keeping this abstract so cars/bikes/trucks can just plug in their type strings later
public abstract class Vehicle {
    private final String licensePlate;
    private final String vehicleType;

    protected Vehicle(String licensePlate, String vehicleType) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be empty.");
        }
        // forcing uppercase mostly for simple comparisons — proper locale handling can come later
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
