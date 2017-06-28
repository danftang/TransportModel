package MicroModel.tests;

import MicroModel.utilities.SpatialVector;
import MicroModel.utilities.WriteFile;
import MicroModel.roads.CornerSegment;
import MicroModel.roads.RoadSegment;
import MicroModel.signs.PelicanCrossing;
import MicroModel.vehicles.PrototypeVehicle;

import java.util.ArrayList;
import java.util.Arrays;


public class SquareCornerSimulation extends PrototypeSimulation {
    /* Simulation of a fleet driving around a square comprising of CornerSegment roads only
     */

    double squareSideLength;
    int fleetSize;


    public SquareCornerSimulation (double squareSideLength, int fleetSize) {

        this.data = new WriteFile("scripts/dataSquareCorner.dat");
        this.squareSideLength = squareSideLength;
        this.fleetSize = fleetSize;

        initialiseRoad();
        initialiseSignage();
        initialiseFleet();
    }


    public void initialiseRoad () {
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
        joinOrderedSegments();
    }


    public void initialiseSignage () {
        /* Initialise some traffic lights
         */
        // Let's place a traffic light a third of the way along each segment
        for (RoadSegment road: roadSegments) {
            PelicanCrossing signal = new PelicanCrossing(road.segLength/3.,10, 2, 10, false);
            road.signage.add(signal);
        }
    }


    public void initialiseFleet () {
         /* Initialise a fleet of n vehicles
         */
        double spacing = roadSegments.get(0).segLength / fleetSize;

        for (int i=0; i<fleetSize; i++) {
            double position = spacing * i;
            double velocity = Math.random() * (20 * 1000 / 3600);         // Set some random speed between 0 & 40 kmh^-1
            PrototypeVehicle vehicle = new PrototypeVehicle(position, velocity);
            fleet.add(vehicle);
            roadSegments.get(0).traffic.add(vehicle);
        }

        // Set the vehicle ahead
        for (RoadSegment road: roadSegments) {
            road.initialiseTraffic();
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


