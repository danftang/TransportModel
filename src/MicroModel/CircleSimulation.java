package MicroModel;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;

public class CircleSimulation {

    ArrayList<PrototypeVehicle> fleet = new ArrayList<>();

    double initialRoadLength;
    double roadRadius;

    int iterationNumber = 0;

    // Set up writefile
    WriteFile data = new WriteFile("scripts/dataCircle.dat");

    public CircleSimulation (double initialRoadLength, int fleetSize) {

        this.initialRoadLength = initialRoadLength;

        initialiseRoad(initialRoadLength);
        initialiseFleet(fleetSize);
    }


    public void initialiseRoad (double initialRoadLength) {
        /* We'll make a road that is circular - just to get the following behaviour going...
         */
        roadRadius = initialRoadLength / (2 * 3.1415926);
    }


    public void initialiseFleet (int n) {
        /* Initialise a fleet of n vehicles
         */
        double spacing = initialRoadLength / n;

        for (int i=0; i<n; i++) {
            double position = spacing * i;
            double velocity = Math.random() * (80 * 1000 / 3600);         // Set some random speed between 0 & 40 kmh^-1
            PrototypeVehicle vehicle = new PrototypeVehicle(position, velocity, Integer.toString(i));
            fleet.add(vehicle);
        }

        // Set the vehicle ahead // TODO need to be automatically computed each dt
        for (int i=0; i<n; i++) {
            PrototypeVehicle vehicleAhead;
            if (i==n-1) {vehicleAhead = fleet.get(0);} else {vehicleAhead = fleet.get(i+1);}
            fleet.get(i).vehicleAhead = vehicleAhead;
        }
    }


    public void plotOutput () {
        /* Prints the vehicle locations to sys out in order for GNU to plot
         */

        for (PrototypeVehicle vehicle: fleet) {
            double noLaps = vehicle.position / initialRoadLength;
            double noWholeLaps = Math.floor(noLaps);
            double remainingLap = noLaps - noWholeLaps;
            double arcLength = remainingLap * initialRoadLength;
            double arcAngle = arcLength / roadRadius;
            double xPos = roadRadius * Math.sin(arcAngle);
            double yPos = roadRadius * Math.cos(arcAngle);
            data.writeToFile(String.format(Double.toString(xPos) + " " + Double.toString(yPos)));
        }
    }


    public void step (double dt) {
        /* Step forwards by dt
         */
        // TODO this is nasty ass

        ArrayList<Double> distances = new ArrayList<>();
        for (PrototypeVehicle vehicle: fleet) {
            distances.add(vehicle.position);
        }
        Collections.sort(distances);
        ArrayList<PrototypeVehicle> order = new ArrayList<>();
        for (double distance: distances) {
            for (PrototypeVehicle vehicle: fleet) {
                if (vehicle.position == distance) {
                    order.add(vehicle);
                }
            }
        }
        System.out.println(order.size());
//        for (PrototypeVehicle vehicle: order) {
//            System.out.println(vehicle.identification);
//        }

        for (int i=0; i<order.size(); i++) {
            PrototypeVehicle vehicleAhead;
            if (i==order.size()-1) {vehicleAhead = order.get(0);} else {vehicleAhead = order.get(i+1);
            }
            order.get(i).vehicleAhead = vehicleAhead;
        }


        for (PrototypeVehicle vehicle: fleet) {
            vehicle.step(dt);
        }
        plotOutput();
        data.writeToFile("\n \n");
        iterationNumber += 1;
    }


    public void run (double dt, double simDuration) {
        /* Run the sim
         */

//        for (PrototypeVehicle vehicle : fleet) {
//            System.out.println(vehicle.identification + " " + vehicle.vehicleAhead.identification);
//        }

        for (int i=0; i*dt<=simDuration; i++) {
            step(dt);
        }
    }


    public static void main (String[] args) {

        // Simulation settings
        double initialRoadLength = 500;
        int fleetSize = 10;
        double dt = 0.1;
        double simDuration = 50;//0.5;

        // Setup a new simulation
        CircleSimulation sim = new CircleSimulation(initialRoadLength, fleetSize);

        // Run simulation
        sim.run(dt, simDuration);
    }

}
