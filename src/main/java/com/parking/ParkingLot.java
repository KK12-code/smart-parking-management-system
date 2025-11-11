package com.parking;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton parking lot that owns and manages all parking spots.
 */
public class ParkingLot {

    private static final int DEFAULT_SPOT_COUNT = 10;
    private static final ParkingLot INSTANCE = new ParkingLot(DEFAULT_SPOT_COUNT);

    private final List<ParkingSpot> parkingSpots;

    private ParkingLot(int numberOfSpots) {
        this.parkingSpots = new ArrayList<>();
        for (int i = 1; i <= numberOfSpots; i++) {
            parkingSpots.add(new ParkingSpot(i));
        }
    }

    public static ParkingLot getInstance() {
        return INSTANCE;
    }

    /**
     * Attempts to park the provided vehicle in the first available spot.
     */
    public synchronized boolean parkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : parkingSpots) {
            if (spot.isAvailable() && spot.parkVehicle(vehicle)) {
                handlePaymentPlaceholder(vehicle);
                persistParkingDataPlaceholder("PARK", vehicle, spot);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a vehicle based on its license plate, freeing up the spot.
     */
    public synchronized boolean removeVehicle(String licensePlate) {
        for (ParkingSpot spot : parkingSpots) {
            Vehicle parked = spot.getVehicle();
            if (parked != null && parked.getLicensePlate().equalsIgnoreCase(licensePlate)) {
                spot.removeVehicle();
                persistParkingDataPlaceholder("REMOVE", parked, spot);
                return true;
            }
        }
        return false;
    }

    /**
     * Displays the current state of all parking spots.
     */
    public void displayStatus() {
        System.out.println("\n--- Parking Lot Status ---");
        for (ParkingSpot spot : parkingSpots) {
            if (spot.isAvailable()) {
                System.out.printf("Spot %d: Available%n", spot.getId());
            } else {
                Vehicle vehicle = spot.getVehicle();
                System.out.printf("Spot %d: Occupied by %s (%s)%n",
                        spot.getId(),
                        vehicle.getVehicleType(),
                        vehicle.getLicensePlate());
            }
        }
        System.out.println("--------------------------\n");
    }

    private void handlePaymentPlaceholder(Vehicle vehicle) {
        // Placeholder hook: replace with real payment processor later.
        new Payment().process(vehicle);
    }

    private void persistParkingDataPlaceholder(String action, Vehicle vehicle, ParkingSpot spot) {
        // Placeholder hook: replace with real persistence (file, DB, etc.) later.
        new Ticket(action, vehicle, spot.getId()).saveToFile();
    }
}
