package com.parking;

import java.util.Scanner;

// figured I'd stub payments out now so the integration point is obvious
// finally talking to the user so we can simulate cash/card confirmation
public class Payment {

    private final Scanner scanner;
    private final ParkingLot parkingLot;

    public Payment() {
        this(new Scanner(System.in));
    }

    public Payment(Scanner scanner) {
        this(scanner, ParkingLot.getInstance());
    }

    public Payment(Scanner scanner, ParkingLot parkingLot) {
        this.scanner = scanner;
        this.parkingLot = parkingLot;
    }

    public void process(Vehicle vehicle) {
        System.out.printf("Processing payment for %s (%s).%n",
                vehicle.getVehicleType(),
                vehicle.getLicensePlate());
        // eventually this might return a receipt object instead of just printing
    }

    public void processPayment(double amount) {
        processPayment(amount, null);
    }

    public void processPayment(double amount, String licensePlate) {
        System.out.printf("Amount due: $%.2f%n", amount);
        while (true) {
            System.out.print("Select payment type (Cash/Card): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if ("cash".equals(choice) || "card".equals(choice)) {
                // this part feels a bit repetitive, might refactor later with enums
                System.out.printf("%s payment confirmed. Thanks for your business!%n",
                        Character.toUpperCase(choice.charAt(0)) + choice.substring(1));
                logPayment(licensePlate, amount);
                break;
            }
            System.out.println("Invalid choice, please type Cash or Card.");
        }
        // maybe add exception handling later when we integrate with a real payment gateway
    }

    private void logPayment(String licensePlate, double amount) {
        if (parkingLot == null) {
            return;
        }
        try {
            parkingLot.logPaymentSuccess(licensePlate == null ? "UNKNOWN" : licensePlate.trim().toUpperCase(), amount);
        } catch (Exception ignored) {
            // logging failure shouldn't block the driver from leaving, so swallowing it intentionally
        }
    }
}
