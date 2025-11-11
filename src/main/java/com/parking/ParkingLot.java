package com.parking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// keeping this as a singleton so the CLI always talks to the same parking lot instance
public class ParkingLot {

    private static final int DEFAULT_SPOT_COUNT = 10;
    // eager initialization is overkill but avoids null checks all over the place
    private static final ParkingLot INSTANCE = new ParkingLot(resolveConfiguredCapacity());

    private final List<ParkingSpot> parkingSpots;

    private ParkingLot(int numberOfSpots) {
        this.parkingSpots = new ArrayList<>();
        for (int i = 1; i <= numberOfSpots; i++) {
            parkingSpots.add(new ParkingSpot(i));
        }
        // could load spot info from a config file later instead of hardcoding
        // ^ finally hooked into config.txt but leaving the reminder because there is still room for a richer schema
    }

    public static ParkingLot getInstance() {
        return INSTANCE;
    }

    /**
     * Attempts to park the provided vehicle in the first available spot.
     */
    public synchronized boolean parkVehicle(Vehicle vehicle) {
        // scanning sequentially is fine for tiny lots; bigger setups probably need indexing
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
        // maybe should use a map later if lookups by plate become hot paths
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

    private static int resolveConfiguredCapacity() {
        Path configPath = Paths.get("src", "resources", "config.txt");
        try {
            if (Files.exists(configPath)) {
                String rawValue = Files.readString(configPath).trim();
                if (!rawValue.isEmpty()) {
                    int configured = Integer.parseInt(rawValue);
                    if (configured > 0) {
                        return configured;
                    }
                }
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Config read failed, sticking with default size: " + ex.getMessage());
        }
        return DEFAULT_SPOT_COUNT;
    }

    @SuppressWarnings("unused")
    private void handlePaymentPlaceholder(Vehicle vehicle) {
        // calling the payment placeholder so we remember to plug in billing logic later
        // Main now takes over actual payments, but I'm keeping the note here as a breadcrumb.
    }

    @SuppressWarnings("unused")
    private void persistParkingDataPlaceholder(String action, Vehicle vehicle, ParkingSpot spot) {
        // logging to the console for now — eventually this should hit a database/file
        // the Ticket class handles the heavy lifting, so no duplicate work here anymore.
    }
}
