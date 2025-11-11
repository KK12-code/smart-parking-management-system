package com.parking;

/**
 * Placeholder class to demonstrate where payment handling will live.
 * You can later connect this to a payment gateway or billing module.
 */
public class Payment {

    public void process(Vehicle vehicle) {
        // TODO: Implement payment calculation and gateway interaction.
        System.out.printf("Processing payment for %s (%s).%n",
                vehicle.getVehicleType(),
                vehicle.getLicensePlate());
    }
}
