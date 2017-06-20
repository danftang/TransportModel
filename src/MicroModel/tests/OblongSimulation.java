package MicroModel.tests;

import MicroModel.roads.CornerSegment;
import MicroModel.roads.RoadSegment;
import MicroModel.roads.StraightSegment;
import MicroModel.signs.PelicanCrossing;
import MicroModel.utilities.SpatialVector;
import MicroModel.utilities.WriteFile;
import MicroModel.vehicles.PrototypeVehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class OblongSimulation extends PrototypeSimulation {
    /* Simulation to test corners connected to straights to make an oblong
     */

    double oblongWidth;
    double oblongHeight;
    int fleetSize;


    public OblongSimulation (double oblongWidth, double oblongHeight, int fleetSize) {

        this.oblongWidth = oblongWidth;
        this.oblongHeight = oblongHeight;
        this.fleetSize = fleetSize;

        data = new WriteFile("scripts/Oblong.dat");

        initialiseRoad();
        initialiseSignage();
        initialiseFleet();
    }


    public void initialiseRoad () {

        double turningRadius = 10;

        // List of the mid-way points along each side - where one junction transitions to the next
        ArrayList<SpatialVector> segmentTransfers = new ArrayList<>(Arrays.asList(
                new SpatialVector(oblongWidth / 3., oblongHeight, 0),
                new SpatialVector(2*oblongWidth / 3., oblongHeight, 0),
                new SpatialVector(oblongWidth, oblongHeight / 2., 0),
                new SpatialVector(2*oblongWidth / 3., 0, 0),
                new SpatialVector(oblongWidth / 3., 0, 0),
                new SpatialVector(0, oblongHeight / 2., 0)
        ));

        // List of the corner points of the road-square
        ArrayList<SpatialVector> segmentCorners = new ArrayList<>(Arrays.asList(
                new SpatialVector(oblongWidth, oblongHeight, 0),
                new SpatialVector(oblongWidth, 0, 0),
                new SpatialVector(0, 0, 0),
                new SpatialVector(0, oblongHeight, 0)
        ));

        // Populate the list of road segments with... road segments.
        roadSegments.add(new StraightSegment(segmentTransfers.get(0), segmentTransfers.get(1)));
        roadSegments.add(new CornerSegment(segmentTransfers.get(1), segmentTransfers.get(2), segmentCorners.get(0), turningRadius));
        roadSegments.add(new CornerSegment(segmentTransfers.get(2), segmentTransfers.get(3), segmentCorners.get(1), turningRadius));
        roadSegments.add(new StraightSegment(segmentTransfers.get(3), segmentTransfers.get(4)));
        roadSegments.add(new CornerSegment(segmentTransfers.get(4), segmentTransfers.get(5), segmentCorners.get(2), turningRadius));
        roadSegments.add(new CornerSegment(segmentTransfers.get(5), segmentTransfers.get(0), segmentCorners.get(3), turningRadius));

        // Connect the segments together by telling each one which road is feeding inward and accepting outward traffic
        joinOrderedSegments();
    }


    public void initialiseSignage () {
        /* Initialise some traffic lights
         */
        // Let's place a traffic light a third of the way along each segment
        for (RoadSegment road: roadSegments) {
            PelicanCrossing signal = new PelicanCrossing(road.segLength / 3., 10, 2, 10, false);
            road.signage.add(signal);
        }
    }

    public void initialiseFleet() {
        /* Initialise a randomly spaced fleet of cars
         */

        for (int i=0; i<fleetSize; i++) {
            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(roadSegments.size());
            RoadSegment road = roadSegments.get(index);
            double distance = randomGenerator.nextDouble() * road.segLength;
            double velocity = randomGenerator.nextDouble() * (20 * 1000 / 3600); // Set some random speed between 0 & 40 kmh^-1
            PrototypeVehicle vehicle = new PrototypeVehicle(distance, velocity, Integer.toString(i));
            fleet.add(vehicle);
            road.traffic.add(vehicle);
        }

        // Set the vehicle ahead
        for (RoadSegment road: roadSegments) {
            road.initialiseTraffic();
        }
    }


    public static void main (String[] args) {

        double oblongWidth = 300;
        double oblongHeight = 200;
        int fleetSize = 10;
        double dt = 0.1;
        double simDuration = 100;

        OblongSimulation sim = new OblongSimulation(oblongWidth, oblongHeight, fleetSize);

        sim.run(dt, simDuration);
    }


}
