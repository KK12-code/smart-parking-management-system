package com.parking;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// writing a tiny console UI so I can manually test without spinning up a GUI
public class Main {

    private final ParkingLot parkingLot = ParkingLot.getInstance();
    private final Scanner scanner = new Scanner(System.in);
    private final Payment payment = new Payment(scanner);
    private final Map<String, Ticket> activeTickets = new HashMap<>();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("Welcome to the Smart Parking Management System!");
        boolean exit = false;

        while (!exit) {
            // looping until user bails out — no background threads so this is fine
            printMenu();
            int choice = readIntInput("Choose an option (1-4): ");

            switch (choice) {
                case 1 -> parkVehicleFlow();
                case 2 -> removeVehicleFlow();
                case 3 -> parkingLot.displayStatus();
                case 4 -> exit = true;
                default -> System.out.println("Please choose a valid option (1-4).");
            }
        }

        System.out.println("Thank you for using the system. Goodbye!");
        scanner.close(); // keeping scanner scoped to this class so we can close it here
    }

    private void printMenu() {
        // menu text is plain ASCII so it renders nicely even in basic terminals
        System.out.println("""
                ------------------------------
                1. Park a vehicle
                2. Remove a vehicle
                3. Display parking lot status
                4. Exit
                ------------------------------""");
    }

    private void parkVehicleFlow() {
        String licensePlate = readStringInput("Enter vehicle license plate: ");
        Vehicle vehicle = chooseVehicleType(licensePlate);

        boolean parked;
        try {
            parked = parkingLot.parkVehicle(vehicle);
        } catch (Exception ex) {
            System.out.println("Had trouble parking the vehicle. Please try again once the system settles down.");
            return;
        }

        if (parked) {
            Ticket ticket = Ticket.generateTicket(vehicle);
            activeTickets.put(vehicle.getLicensePlate(), ticket);
            System.out.println("Vehicle parked successfully!");
            // stashing the ticket so we can settle payment when the driver heads out
            System.out.printf("Entry time recorded at %s%n", ticket.getEntryTime());
        } else {
            // future idea: offer to join a waitlist instead of just printing this
            System.out.println("Parking lot is full. Please try again later.");
        }
    }

    private void removeVehicleFlow() {
        String licensePlate = readStringInput("Enter license plate to remove: ");
        // not doing fancy lookup — just asking for plate and letting the lot handle it
        String normalizedPlate = licensePlate.trim().toUpperCase();
        boolean removed;
        try {
            removed = parkingLot.removeVehicle(normalizedPlate);
        } catch (Exception ex) {
            System.out.println("Could not complete the removal right now. Please try again in a moment.");
            return;
        }

        if (removed) {
            Ticket ticket = activeTickets.remove(normalizedPlate);
            if (ticket == null) {
                System.out.println("Vehicle removed, but no ticket data was tracked (probably a manual override).");
                return;
            }
            double cost = ticket.closeTicket();
            System.out.printf("Parking duration cost for %s: $%.2f%n", normalizedPlate, cost);
            // piping the total over to the Payment helper so the user can settle up right away
            payment.processPayment(cost, normalizedPlate);
            ticket.saveToFile();
            System.out.println("Vehicle removed. Spot is now available.");
        } else {
            System.out.println("Vehicle not found. Please verify the license plate.");
        }
    }

    private Vehicle chooseVehicleType(String licensePlate) {
        while (true) {
            System.out.println("""
                    Select vehicle type:
                    1. Car
                    2. Bike
                    3. Truck""");

            int choice = readIntInput("Enter type number: ");

            Vehicle vehicle = switch (choice) {
                case 1 -> new Car(licensePlate);
                case 2 -> new Bike(licensePlate);
                case 3 -> new Truck(licensePlate);
                default -> null;
            };

            if (vehicle != null) {
                return vehicle;
            }
            System.out.println("Invalid vehicle type. Let's try that again.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value < 0) {
                    // keeping validation gentle so new users don't rage quit
                    System.out.println("Please enter a positive number.");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private String readStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                // gently nudging rather than scolding — folks type fast on kiosks
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }
            return input;
        }
    }
}
