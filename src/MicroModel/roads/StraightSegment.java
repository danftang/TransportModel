package MicroModel.roads;

import MicroModel.SpatialVector;

public class StraightSegment extends RoadSegment {

    public StraightSegment (SpatialVector segStart, SpatialVector segEnd) {

        this.segStart = segStart;
        this.segEnd = segEnd;

        this.segLength = segStart.sub(segEnd).length();
    }
}
