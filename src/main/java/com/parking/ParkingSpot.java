package com.parking;

// just a dumb spot right now — no notion of size or pricing tiers yet
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
        // not cloning or validating vehicle type for now, assuming caller already checked that
        this.vehicle = vehicle;
        return true;
    }

    /**
     * Removes the parked vehicle (if any) and returns it so callers can inspect it.
     */
    public Vehicle removeVehicle() {
        Vehicle removed = this.vehicle;
        // clearing the reference immediately so the spot is marked free even if caller forgets
        this.vehicle = null;
        return removed;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}
