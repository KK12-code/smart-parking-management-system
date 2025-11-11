package com.parking;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// acting as a stub logger right now so I can swap in real persistence later
// finally wiring it up to behave like a proper ticket while keeping things approachable
public class Ticket {

    private static final double HOURLY_RATE = 5.0; // just a flat rate for now — real pricing can plug in later
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String licensePlate;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double cost;

    private Ticket(Vehicle vehicle) {
        this.licensePlate = vehicle.getLicensePlate();
        this.entryTime = LocalDateTime.now();
        // timestamping here keeps the calling code nice and short
    }

    public static Ticket generateTicket(Vehicle vehicle) {
        // keeping the factory so callers don't have to remember to set timestamps themselves
        return new Ticket(vehicle);
    }

    public double closeTicket() {
        if (exitTime != null) {
            return cost;
        }
        exitTime = LocalDateTime.now();
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        double billableHours = Math.max(1.0, minutes / 60.0); // minimum one hour to keep the math simple
        cost = Math.round(billableHours * HOURLY_RATE * 100.0) / 100.0;
        return cost;
    }

    public void saveToFile() {
        Path resourcesDir = Paths.get("src", "resources");
        Path ticketsFile = resourcesDir.resolve("tickets.txt");
        double finalCost = (exitTime == null) ? closeTicket() : cost;

        try {
            Files.createDirectories(resourcesDir);
            String record = String.format("%s | %s | %s | $%.2f%n",
                    licensePlate,
                    FORMATTER.format(entryTime),
                    FORMATTER.format(exitTime),
                    finalCost);
            // still printing to console so I can see activity while developing
            System.out.printf("Saving ticket: %s", record);
            // using BufferedWriter here just to keep it simple and append-friendly
            try (BufferedWriter writer = Files.newBufferedWriter(
                    ticketsFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(record);
            }
        } catch (IOException e) {
            System.err.println("Failed to persist ticket data: " + e.getMessage());
        }
    }

    public double getCost() {
        return cost;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
}
