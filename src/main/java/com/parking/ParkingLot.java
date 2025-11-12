package com.parking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// keeping this as a singleton so the CLI always talks to the same parking lot instance
public class ParkingLot {

    private static final int DEFAULT_SPOT_COUNT = 10;
    private static final Path RESOURCES_DIR = Paths.get("src", "resources");
    private static final Path STATE_FILE = RESOURCES_DIR.resolve("lot_state.txt");
    private static final Path LOG_FILE = RESOURCES_DIR.resolve("logs.txt");
    private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // eager initialization is overkill but avoids null checks all over the place
    private static final ParkingLot INSTANCE = new ParkingLot(resolveConfiguredCapacity());

    private final List<ParkingSpot> parkingSpots;
    private final boolean persistenceEnabled;

    private ParkingLot(int numberOfSpots) {
        this(numberOfSpots, true);
    }

    private ParkingLot(int numberOfSpots, boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
        this.parkingSpots = new ArrayList<>();
        for (int i = 1; i <= numberOfSpots; i++) {
            parkingSpots.add(new ParkingSpot(i));
        }
        // could load spot info from a config file later instead of hardcoding
        // ^ finally hooked into config.txt but leaving the reminder because there is still room for a richer schema
        if (this.persistenceEnabled) {
            loadPersistedState();
        }
    }

    public static ParkingLot getInstance() {
        return INSTANCE;
    }

    // building a throwaway factory so the SystemTest helper can spin up isolated lots without polluting files
    static ParkingLot createEphemeralLot(int numberOfSpots) {
        return new ParkingLot(numberOfSpots, false);
    }

    /**
     * Attempts to park the provided vehicle in the first available spot.
     */
    public synchronized boolean parkVehicle(Vehicle vehicle) {
        // scanning sequentially is fine for tiny lots; bigger setups probably need indexing
        for (ParkingSpot spot : parkingSpots) {
            if (spot.isAvailable() && spot.parkVehicle(vehicle)) {
                persistCurrentState();
                logAction("PARK", String.format("%s (%s) grabbed spot %d", vehicle.getVehicleType(), vehicle.getLicensePlate(), spot.getId()));
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
                persistCurrentState();
                logAction("REMOVE", String.format("%s left spot %d", parked.getLicensePlate(), spot.getId()));
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

    public synchronized void logPaymentSuccess(String licensePlate, double amount) {
        logAction("PAYMENT", String.format("%s settled $%.2f", licensePlate == null ? "UNKNOWN" : licensePlate, amount));
    }

    // trying to persist data so state isn't lost on exit, so this reloads whatever we stored previously
    private void loadPersistedState() {
        try {
            ensureStateFileReady();
        } catch (IOException ioException) {
            System.err.println("Failed to prepare parking state file: " + ioException.getMessage());
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(STATE_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split("\\|");
                if (parts.length != 3) {
                    System.err.println("Skipping malformed parking record: " + trimmed);
                    continue;
                }

                int spotId;
                try {
                    spotId = Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException nfe) {
                    System.err.println("Bad spot number in saved data: " + parts[0]);
                    continue;
                }

                Vehicle reconstructed = recreateVehicle(parts[1].trim(), parts[2].trim());
                if (reconstructed == null) {
                    System.err.println("Unknown vehicle type in saved data: " + parts[1]);
                    continue;
                }

                ParkingSpot targetSpot = findSpotById(spotId);
                if (targetSpot == null) {
                    System.err.println("Saved spot " + spotId + " exceeds current lot size. Ignoring entry.");
                    continue;
                }
                if (!targetSpot.isAvailable()) {
                    // this shouldn't happen but I'd rather overwrite than leave the file inconsistent
                    targetSpot.removeVehicle();
                }
                targetSpot.parkVehicle(reconstructed);
            }
        } catch (IOException ioException) {
            System.err.println("Could not read persisted parking data: " + ioException.getMessage());
            recreateStateFile();
        }
    }

    // trying to persist data so state isn't lost on exit
    private void persistCurrentState() {
        if (!persistenceEnabled) {
            return;
        }
        try {
            ensureStateFileReady();
            try (BufferedWriter writer = Files.newBufferedWriter(
                    STATE_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                for (ParkingSpot spot : parkingSpots) {
                    if (spot.isAvailable()) {
                        continue;
                    }
                    Vehicle vehicle = spot.getVehicle();
                    // probably should refactor this if it grows bigger, but a simple pipe-delimited line works for now
                    writer.write(String.format("%d|%s|%s%n",
                            spot.getId(),
                            vehicle.getVehicleType(),
                            vehicle.getLicensePlate()));
                }
            }
        } catch (IOException ioException) {
            System.err.println("Failed to persist parking data: " + ioException.getMessage());
        }
    }

    private void ensureStateFileReady() throws IOException {
        Files.createDirectories(RESOURCES_DIR);
        if (Files.notExists(STATE_FILE)) {
            Files.createFile(STATE_FILE);
        }
        if (Files.notExists(LOG_FILE)) {
            Files.createFile(LOG_FILE);
        }
    }

    private void recreateStateFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(
                STATE_FILE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("");
        } catch (IOException ioException) {
            System.err.println("Could not recreate parking state file: " + ioException.getMessage());
        }
    }

    private ParkingSpot findSpotById(int spotId) {
        for (ParkingSpot spot : parkingSpots) {
            if (spot.getId() == spotId) {
                return spot;
            }
        }
        return null;
    }

    private Vehicle recreateVehicle(String type, String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "car" -> new Car(licensePlate);
            case "bike" -> new Bike(licensePlate);
            case "truck" -> new Truck(licensePlate);
            default -> null;
        };
    }

    private void logAction(String action, String message) {
        if (!persistenceEnabled) {
            return;
        }
        try {
            Files.createDirectories(RESOURCES_DIR);
            String entry = String.format("%s | %s | %s%n",
                    LOG_TIME.format(LocalDateTime.now()),
                    action,
                    message);
            Files.writeString(
                    LOG_FILE,
                    entry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ioException) {
            System.err.println("Unable to append to log file: " + ioException.getMessage());
        }
    }
}
