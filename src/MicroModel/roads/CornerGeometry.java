package MicroModel.roads;

import MicroModel.SpatialVector;
import MicroModel.WriteFile;


public class CornerGeometry {
    /* Computes the geometry of a rounded corner with inlet and outlet straights
     */

    // Initial details
    SpatialVector segStart;
    SpatialVector segEnd;
    SpatialVector cornerLocation;
    double cornerRad;

    // Normalised vectors for the inway / outway
    SpatialVector vecIn;
    SpatialVector vecOut;

    // Lengths of the inway. outway, arc and segment
    double lenIn;
    double lenOut;
    double lenArc;
    double totalLength;

    // Arc and angles
    double cornerAngle;
    double arcAngle;
    SpatialVector cornerStart;
    SpatialVector cornerEnd;
    SpatialVector arcCentre;


    public CornerGeometry (SpatialVector segStart, SpatialVector segEnd, SpatialVector cornerLocation,
                           double cornerRad) {

        this.segStart = segStart;
        this.segEnd = segEnd;
        this.cornerLocation = cornerLocation;
        this.cornerRad = cornerRad;

        this.vecIn = segStart.sub(cornerLocation).normalize();
        this.vecOut = segEnd.sub(cornerLocation).normalize();

        this.cornerAngle = vecIn.angleWith(vecOut);
        this.arcAngle = Math.PI - cornerAngle;
        this.lenArc = cornerRad * arcAngle;

        double trim = cornerRad / Math.tan(cornerAngle / 2.0);
        this.lenIn = segStart.sub(cornerLocation).length() - trim;
        this.lenOut = segEnd.sub(cornerLocation).length() - trim;
        this.totalLength = lenIn + lenArc + lenOut;

        this.cornerStart = segStart.sub(vecIn.scale(lenIn));
        this.cornerEnd = cornerLocation.add(vecOut.scale(trim));

        SpatialVector vecInPerp = vecIn.perpendicular();
        arcCentre = cornerStart.add(vecInPerp.scale(cornerRad));

    }


    // TODO check my vecIn and vecOut are not the reverse polarity to what they ought...
    public SpatialVector convertDistanceToPoint (double distance) {
        /* Convert distance along a road segment into a point in space
         */
        // Check if still on inway
        if (distance <= lenIn) {
            SpatialVector test = segStart.sub(vecIn.scale(distance));
            return test;
        }
        // Check if on the arc
        else if (distance < lenIn + lenArc) {
            double dist_on_arc = distance - lenIn;
            double angle = -dist_on_arc / cornerRad;
            double x_ = arcCentre.x + Math.cos(angle) * (cornerStart.x - arcCentre.x)
                    - Math.sin(angle) * (cornerStart.y - arcCentre.y);
            double y_ = arcCentre.y + Math.sin(angle) * (cornerStart.x - arcCentre.x)
                    + Math.cos(angle) * (cornerStart.y - arcCentre.y);
            return new SpatialVector(x_, y_, 0.0);
        }
        // Failing that we're on the outway
        else {
            double distOnSeg = distance - (lenIn + lenArc);
            return cornerEnd.add(vecOut.scale(distOnSeg));
        }
    }


    public static void main (String args[]) {

        SpatialVector start = new SpatialVector(8, 6, 0);
        SpatialVector end = new SpatialVector(16, 12, 0);
        SpatialVector corner = new SpatialVector(9, 13, 0);
        double radius = 4.0;

        // Set up writefile
        WriteFile data = new WriteFile("scripts/cornerPoints.dat");

        CornerGeometry cg = new CornerGeometry(start, end, corner, radius);

        for (double dist=0; dist <= cg.totalLength; dist += cg.totalLength/100.) {
            SpatialVector point = cg.convertDistanceToPoint(dist);
            data.writeToFile(point.x + " " + point.y);
        }

        boolean bugFix = false;
        if (bugFix) {
            System.out.println("vecIn " + cg.vecIn.x + " " + cg.vecIn.y);
            System.out.println("vecOut " + cg.vecOut.x + " " + cg.vecOut.y);
            System.out.println("lenIn " + cg.lenIn);
            System.out.println("lenOut " + cg.lenOut);
            System.out.println("lenArc " + cg.lenArc);
            System.out.println("totalLength " + cg.totalLength);
            System.out.println("cornerAngle " + cg.cornerAngle);
            System.out.println("arcAngle " + cg.arcAngle);
            System.out.println("cornerStart " + cg.cornerStart.x + " " + cg.cornerStart.y);
            System.out.println("cornerEnd " + cg.cornerEnd.x + " " + cg.cornerEnd.y);
            System.out.println("arcCentre " + cg.arcCentre.x + " " + cg.arcCentre.y);
        }
    }
}
