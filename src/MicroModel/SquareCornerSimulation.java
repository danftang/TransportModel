package MicroModel;

import MicroModel.roads.CornerSegment;
import MicroModel.roads.RoadSegment;

import java.util.ArrayList;
import java.util.Arrays;


public class SquareCornerSimulation {
    /* Simulation of a fleet driving around a square comprising of CornerSegment roads only
     */

    ArrayList<PrototypeVehicle> fleet = new ArrayList<>();

    double roadLength = 0;

    // Initialise a list of road segments
    ArrayList<RoadSegment> roadSegments = new ArrayList<>();

    int iterationNumber = 0;

    // Set up writefile
    WriteFile data = new WriteFile("scripts/dataSquareCorner.dat");


    public SquareCornerSimulation (double squareSideLength, int fleetSize) {

        initialiseRoad(squareSideLength);
        initialiseFleet(fleetSize);
    }


    public void initialiseRoad (double squareSideLength) {
        /* We're building this road in 4 corner parts; assemble them here
         */
        double turningRadius = 10;

        // List of the mid-way points along each side - where one junction transitions to the next
        ArrayList<SpatialVector> segmentTransfers = new ArrayList<>(Arrays.asList(
                new SpatialVector(squareSideLength / 2., squareSideLength, 0),
                new SpatialVector(squareSideLength, squareSideLength / 2., 0),
                new SpatialVector(squareSideLength / 2., 0, 0),
                new SpatialVector(0, squareSideLength / 2., 0)
        ));

        // List of the corner points of the road-square
        ArrayList<SpatialVector> segmentCorners = new ArrayList<>(Arrays.asList(
                new SpatialVector(squareSideLength, squareSideLength, 0),
                new SpatialVector(squareSideLength, 0, 0),
                new SpatialVector(0, 0, 0),
                new SpatialVector(0, squareSideLength, 0)
        ));

        // Populate the list of road segments with... road segments.
        for (int i = 0; i < segmentCorners.size(); i++) {
            int segEndIndex;
            if (i == segmentTransfers.size() - 1) {
                segEndIndex = 0;
            } else {
                segEndIndex = i + 1;
            }
            roadSegments.add(new CornerSegment(segmentTransfers.get(i), segmentTransfers.get(segEndIndex),
                    segmentCorners.get(i), turningRadius));
        }

        // Connect the segments together by telling each one which road is feeding inward and accepting outward traffic
        for (int i = 0; i < roadSegments.size(); i++) {
            int segInIndex;
            int segOutIndex;
            if (i == roadSegments.size() - 1) {
                segInIndex = i - 1;
                segOutIndex = 0;
            } else if (i == 0) {
                segInIndex = roadSegments.size() - 1;
                segOutIndex = i + 1;
            } else {
                segInIndex = i - 1;
                segOutIndex = i + 1;
            }
            roadSegments.get(i).connectedSegments.add(roadSegments.get(segInIndex));
            roadSegments.get(i).connectedSegments.add(roadSegments.get(segOutIndex));

            // And tot up the complete road length
            roadLength += roadSegments.get(i).segLength;
        }

    }

    public void initialiseFleet (int n) {
         /* Initialise a fleet of n vehicles
         */
        double spacing = roadSegments.get(0).segLength / n;

        for (int i=0; i<n; i++) {
            double position = spacing * i;
            double velocity = Math.random() * (20 * 1000 / 3600);         // Set some random speed between 0 & 40 kmh^-1
            PrototypeVehicle vehicle = new PrototypeVehicle(position, velocity, Integer.toString(i));
            fleet.add(vehicle);
            roadSegments.get(0).traffic.add(vehicle);
        }

        // Set the vehicle ahead
        for (RoadSegment road: roadSegments) {
            road.initialiseTraffic();
        }
    }


    private void plotOutput (double dt) {

        Double time = dt * iterationNumber;
        for (PrototypeVehicle vehicle: fleet) {
            SpatialVector pos = vehicle.getLocation();
            data.writeToFile(String.format(Double.toString(time) +" "+
                                           Double.toString(pos.x) +" "+
                                           Double.toString(pos.y)));
        }
        data.writeToFile("\n \n");
    }


    public void step (double dt) {
        /* Step forwards by dt
         */

        // Update the vehicles for the given timestep
        for (PrototypeVehicle vehicle: fleet) {
            vehicle.step(dt);
        }

        // Update the road segments to assign to each vehicle the vehicle-ahead and its dx from it
        for (RoadSegment road: roadSegments) {
            road.updateTraffic();
        }

        plotOutput(dt);
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

        double squareSideLength = 650;
        int fleetSize = 10;
        double dt = 0.1;
        double simDuration = 100;

        SquareCornerSimulation sim = new SquareCornerSimulation(squareSideLength, fleetSize);

        sim.run(dt, simDuration);
    }


}


