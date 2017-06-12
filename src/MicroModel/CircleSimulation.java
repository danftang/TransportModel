package MicroModel;

import java.util.ArrayList;

public class CircleSimulation {

    ArrayList<PrototypeVehicle> fleet = new ArrayList<>();

    double initialRoadLength;
    double roadRadius;

    int iterationNumber = 0;

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
            double velocity = Math.random() * (40 * 1000 / 3600);         // Set some random speed between 0 & 40 kmh^-1
            PrototypeVehicle vehicle = new PrototypeVehicle(position, velocity);
            fleet.add(vehicle);
        }

        // Set the vehicle ahead // TODO need to be automatically computed each dt
        for (int i=0; i<n; i++) {
            PrototypeVehicle vehicleAhead;
            if (i==0) {vehicleAhead = fleet.get(n-1);} else {vehicleAhead = fleet.get(i-1);}
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
            System.out.println(String.format("%d %d", xPos, yPos));
        }
    }


    public void step (double dt) {
        /* Step forwards by dt
         */

        for (PrototypeVehicle vehicle: fleet) {
            vehicle.step(dt);
        }
        plotOutput();
        System.out.println("\n \n");
        iterationNumber += 1;
    }


    public void run (double dt, double simDuration) {
        /* Run the sim
         */

        for (int i=0; i*dt<=simDuration; i++) {
            step(dt);
        }
    }


    public static void main (String[] args) {

        double initialRoadLength = 260;
        int fleetSize = 10;
        double dt = 0.1;
        double simDuration = 10;

        CircleSimulation sim = new CircleSimulation(initialRoadLength, fleetSize);

        sim.run(dt, simDuration);
    }

}
