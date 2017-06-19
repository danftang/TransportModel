package MicroModel.roads;

import MicroModel.PrototypeVehicle;
import MicroModel.SpatialVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

// TODO -> When a vehicle enters a section that has multiple exits, it needs to decide which it'll be taken so that the
// TODO -| (currently hard-coded) "connectedSegments.get(1)..." snippets can be appropriately un-hard coded.


public abstract class RoadSegment {

    // Geometry
    public SpatialVector segStart;
    public SpatialVector segEnd;
    public double segLength;

    // Connecting segments
    public ArrayList<RoadSegment> connectedSegments = new ArrayList<>();
    HashMap<String, RoadSegment> connectionKey = new HashMap<>();

    // Vehicles on the segment
    public LinkedList<PrototypeVehicle> traffic = new LinkedList<>();


    // Methods which extensions to this abstract class must overide:
    public abstract SpatialVector convertPositionToLocation (double position);


    public void initialiseTraffic () {
        /* Initialise the traffic on the road segment by telling each vehicle that this is the segment it is on
         */
        for (PrototypeVehicle vehicle: traffic) {
            vehicle.roadSegment = this;
        }
        updateTraffic();
    }


    public void updateTraffic () {
        /* Do housekeeping with the vehicles on the segment, exchange with a neighbouring segment as needed, let the
        vehicles know the dx to the car in front. */
        // Move traffic onto the next segment if it is progressed beyond the extent of the current one.
        for (PrototypeVehicle vehicle: traffic) {
            if (vehicle.position > segLength) {
                vehicle.position = segLength - vehicle.position;
                traffic.remove(vehicle);
                connectedSegments.get(1).traffic.add(vehicle);
                vehicle.roadSegment = connectedSegments.get(1);
            }
        }

        // Now that everyone's on their correct segment let's sort the list
        Collections.sort(traffic);

        // Tell the traffic who they're following and what the dx is. dx is handled here because it is affected by
        // segmented discontinuity - whereas dv etc can still be computed on the vehicle.
        for (int i=0; i<traffic.size(); i++) {
            if (i == traffic.size()-1) {
                traffic.get(i).vehicleAhead = connectedSegments.get(1).traffic.get(0);
                double distToSegEnd = segLength - traffic.get(i).position;
                traffic.get(i).status.put("dx", distToSegEnd + connectedSegments.get(1).traffic.get(0).position -
                                                connectedSegments.get(1).traffic.get(0).vehicleLength);
            } else {
                traffic.get(i).vehicleAhead = traffic.get(i+1);
                traffic.get(i).status.put("dx", traffic.get(i+1).position - traffic.get(i).position -
                                                traffic.get(i+1).vehicleLength);
            }
        }
    }
}
