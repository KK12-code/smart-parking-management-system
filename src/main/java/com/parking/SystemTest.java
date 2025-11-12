package com.parking;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// tossing in a scrappy test harness so we can sanity check flows without dragging JUnit into the mix
public final class SystemTest {

    private SystemTest() {
        // no instances — just static helpers for quick console demos
    }

    public static void testParkingLotFilling() {
        ParkingLot lot = ParkingLot.createEphemeralLot(2);
        Vehicle first = new Car("TEST100");
        Vehicle second = new Bike("TEST200");
        Vehicle overflow = new Truck("TEST300");

        boolean firstResult = lot.parkVehicle(first);
        boolean secondResult = lot.parkVehicle(second);
        boolean overflowResult = lot.parkVehicle(overflow);
        boolean removeFirst = lot.removeVehicle(first.getLicensePlate());
        boolean reparkOverflow = lot.parkVehicle(overflow);

        boolean passed = firstResult && secondResult && !overflowResult && removeFirst && reparkOverflow;
        printResult("Parking lot filling", passed,
                passed ? "lot respected capacity limits" : "expected rejection once capacity was hit");
    }

    public static void testTicketGeneration() {
        Vehicle tempCar = new Car("TICKET1");
        Ticket ticket = Ticket.generateTicket(tempCar);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double cost = ticket.closeTicket();
        ticket.saveToFile();
        boolean passed = cost >= 5.0;
        printResult("Ticket generation", passed,
                String.format("computed cost $%.2f after quick stay", cost));
    }

    public static void testPaymentFlow() {
        byte[] scriptedInput = "card\n".getBytes(StandardCharsets.UTF_8);
        Scanner scriptedScanner = new Scanner(new ByteArrayInputStream(scriptedInput));
        Payment payment = new Payment(scriptedScanner, ParkingLot.createEphemeralLot(1));
        boolean passed;
        try {
            payment.processPayment(12.50, "SIM999");
            passed = true;
        } catch (Exception ex) {
            passed = false;
        }
        scriptedScanner.close();
        printResult("Payment flow", passed,
                passed ? "automated scanner input hit the happy path" : "payment helper threw an exception");
    }

    public static void runAll() {
        testParkingLotFilling();
        testTicketGeneration();
        testPaymentFlow();
    }

    public static void main(String[] args) {
        runAll();
    }

    private static void printResult(String testName, boolean passed, String details) {
        System.out.printf("[%s] %s - %s%n", passed ? "PASS" : "FAIL", testName, details);
    }
}
