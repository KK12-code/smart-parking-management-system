package com.parking;

import java.time.LocalDateTime;

/**
 * Placeholder for persistence/file storage of parking events.
 */
public class Ticket {

    private final String action;
    private final String licensePlate;
    private final int spotId;
    private final LocalDateTime timestamp;

    public Ticket(String action, Vehicle vehicle, int spotId) {
        this.action = action;
        this.licensePlate = vehicle.getLicensePlate();
        this.spotId = spotId;
        this.timestamp = LocalDateTime.now();
    }

    public void saveToFile() {
        // TODO: Write ticket details to a file or database.
        System.out.printf("Saving ticket: [%s] %s at spot %d (%s)%n",
                action,
                licensePlate,
                spotId,
                timestamp);
    }
}
