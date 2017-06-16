package MicroModel.roads;

import MicroModel.SpatialVector;

import java.util.ArrayList;
import java.util.HashMap;

public class RoadSegment {

    // Geometry
    public SpatialVector segStart;
    public SpatialVector segEnd;
    public double segLength;

    // Connecting segments
    public ArrayList<RoadSegment> connectedSegments = new ArrayList<>();
    HashMap<String, RoadSegment> connectionKey = new HashMap<>();

}
