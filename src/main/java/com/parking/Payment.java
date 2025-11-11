package com.parking;

// figured I'd stub payments out now so the integration point is obvious
public class Payment {

    public void process(Vehicle vehicle) {
        // TODO: Implement payment calculation and gateway interaction.
        System.out.printf("Processing payment for %s (%s).%n",
                vehicle.getVehicleType(),
                vehicle.getLicensePlate());
        // eventually this might return a receipt object instead of just printing
    }
}
