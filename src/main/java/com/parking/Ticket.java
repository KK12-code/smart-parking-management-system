package com.parking;

import java.time.LocalDateTime;

// acting as a stub logger right now so I can swap in real persistence later
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
        // timestamping here keeps the calling code nice and short
    }

    public void saveToFile() {
        // still printing to console so I can see activity while developing
        System.out.printf("Saving ticket: [%s] %s at spot %d (%s)%n",
                action,
                licensePlate,
                spotId,
                timestamp);
    }
}
