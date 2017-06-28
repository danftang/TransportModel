package MicroModel.roads;

import MicroModel.utilities.SpatialVector;


public class StraightSegment extends RoadSegment {
    /* A straight road segment
     */

    public StraightSegment (SpatialVector segStart, SpatialVector segEnd) {

        this.segStart = segStart;
        this.segEnd = segEnd;

        this.segLength = segStart.sub(segEnd).length();
    }


    public SpatialVector convertPositionToLocation (double distance) {
        /* Converts position on a road segment into a cartesian coordinate
         */
        SpatialVector unitVec = segEnd.sub(segStart).normalize();
        return segStart.add(unitVec.scale(distance));
    }
}
