package MicroModel.roads;

import MicroModel.SpatialVector;


public class CornerSegment extends RoadSegment {
    /* Corner road segment
     */

    public CornerGeometry geo;


    public CornerSegment (SpatialVector segStart, SpatialVector segEnd, SpatialVector cornerLocation,
                          double turningRadius) {

        this.segStart = segStart;
        this.segEnd = segEnd;

        geo = new CornerGeometry(segStart, segEnd, cornerLocation, turningRadius);

        this.segLength = geo.totalLength;

        // Connecting segments
        connectionKey.put("incomingSegment", 0);
        connectionKey.put("outgoingSegment", 1);
    }

    public SpatialVector convertPositionToLocation (double distance) {
        /* Converts position on a road segment into a cartesian coordinate
         */
        return geo.convertDistanceToPoint(distance);
    }
}
