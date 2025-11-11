package com.parking;

/**
 * Represents a single parking spot that can hold one vehicle at a time.
 */
public class ParkingSpot {

    private final int id;
    private Vehicle vehicle;

    public ParkingSpot(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isAvailable() {
        return vehicle == null;
    }

    /**
     * Parks the given vehicle if the spot is free.
     *
     * @return true if parking succeeded, false otherwise.
     */
    public boolean parkVehicle(Vehicle vehicle) {
        if (!isAvailable()) {
            return false;
        }
        this.vehicle = vehicle;
        return true;
    }

    /**
     * Removes the parked vehicle (if any) and returns it so callers can inspect it.
     */
    public Vehicle removeVehicle() {
        Vehicle removed = this.vehicle;
        this.vehicle = null;
        return removed;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}
