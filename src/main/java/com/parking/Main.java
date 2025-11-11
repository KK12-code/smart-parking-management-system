package com.parking;

import java.util.Scanner;

/**
 * Simple command-line interface so users can interact with the parking lot.
 */
public class Main {

    private final ParkingLot parkingLot = ParkingLot.getInstance();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("Welcome to the Smart Parking Management System!");
        boolean exit = false;

        while (!exit) {
            printMenu();
            int choice = readIntInput("Choose an option: ");

            switch (choice) {
                case 1 -> parkVehicleFlow();
                case 2 -> removeVehicleFlow();
                case 3 -> parkingLot.displayStatus();
                case 4 -> exit = true;
                default -> System.out.println("Please choose a valid option (1-4).");
            }
        }

        System.out.println("Thank you for using the system. Goodbye!");
        scanner.close();
    }

    private void printMenu() {
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

        if (vehicle == null) {
            System.out.println("Vehicle creation failed. Returning to main menu.");
            return;
        }

        boolean parked = parkingLot.parkVehicle(vehicle);

        if (parked) {
            System.out.println("Vehicle parked successfully!");
        } else {
            System.out.println("Parking lot is full. Please try again later.");
        }
    }

    private void removeVehicleFlow() {
        String licensePlate = readStringInput("Enter license plate to remove: ");
        boolean removed = parkingLot.removeVehicle(licensePlate);

        if (removed) {
            System.out.println("Vehicle removed. Spot is now available.");
        } else {
            System.out.println("Vehicle not found. Please verify the license plate.");
        }
    }

    private Vehicle chooseVehicleType(String licensePlate) {
        System.out.println("""
                Select vehicle type:
                1. Car
                2. Bike
                3. Truck""");

        int choice = readIntInput("Enter type number: ");

        return switch (choice) {
            case 1 -> new Car(licensePlate);
            case 2 -> new Bike(licensePlate);
            case 3 -> new Truck(licensePlate);
            default -> {
                System.out.println("Invalid vehicle type.");
                yield null;
            }
        };
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value < 0) {
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
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
