package MicroModel.roads;

import MicroModel.SpatialVector;
import MicroModel.WriteFile;

public class CornerGeometry {

    // Initial details
    SpatialVector seg_start;
    SpatialVector seg_end;
    SpatialVector corner_location;
    double corner_rad;

    // Normalised vectors for the inway / outway
    SpatialVector vec_in;
    SpatialVector vec_out;

    // Lengths of the inway. outway, arc and segment
    double len_in;
    double len_out;
    double len_arc;
    double total_length;

    // Arc and angles
    double corner_angle;
    double arc_angle;
    SpatialVector corner_start;
    SpatialVector corner_end;
    SpatialVector arc_centre;


    public CornerGeometry (SpatialVector seg_start, SpatialVector seg_end, SpatialVector corner_location,
                           double corner_rad) {

        this.seg_start = seg_start;
        this.seg_end = seg_end;
        this.corner_location = corner_location;
        this.corner_rad = corner_rad;

        this.vec_in = seg_start.sub(corner_location).normalize();
        System.out.println(vec_in.x +" "+ vec_in.y);
        this.vec_out = seg_end.sub(corner_location).normalize();

        this.corner_angle = vec_in.angleWith(vec_out);
        this.arc_angle = Math.PI - corner_angle;
        this.len_arc = corner_rad * arc_angle;

        double trim = corner_rad / Math.tan(corner_angle / 2.0);
        this.len_in = seg_start.sub(corner_location).length() - trim;
        this.len_out = seg_end.sub(corner_location).length() - trim;
        this.total_length = len_in + len_arc + len_out;

        this.corner_start = seg_start.sub(vec_in.scale(len_in));
        this.corner_end = corner_location.add(vec_out.scale(trim));

        SpatialVector vec_in_perp = vec_in.perpendicular();
        arc_centre = corner_start.add(vec_in_perp.scale(corner_rad));

    }


    public SpatialVector convertDistanceToPoint (double distance) {
        /* Convert distance along a road segment into a point in space
         */
        // Check if still on inway
        if (distance <= len_in) {
            SpatialVector test = seg_start.sub(vec_in.scale(distance));
            return test;
        }
        // Check if on the arc
        else if (distance < len_in + len_arc) {
            double dist_on_arc = distance - len_in;
            double angle = -dist_on_arc / corner_rad;
            double x_ = arc_centre.x + Math.cos(angle) * (corner_start.x - arc_centre.x)
                    - Math.sin(angle) * (corner_start.y - arc_centre.y);
            double y_ = arc_centre.y + Math.sin(angle) * (corner_start.x - arc_centre.x)
                    + Math.cos(angle) * (corner_start.y - arc_centre.y);
            return new SpatialVector(x_, y_, 0.0);
        }
        // Failing that we're on the outway
        else {
            double dist_on_seg = distance - len_in - len_arc;
            return seg_end.sub(vec_out.scale(dist_on_seg));
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

        for (double dist=0; dist <= cg.total_length; dist += cg.total_length/100.) {
            SpatialVector point = cg.convertDistanceToPoint(dist);
            data.writeToFile(point.x + " " + point.y);
        }

        boolean bugFix = false;
        if (bugFix) {
            System.out.println("vec_in " + cg.vec_in.x + " " + cg.vec_in.y);
            System.out.println("vec_out " + cg.vec_out.x + " " + cg.vec_out.y);
            System.out.println("len_in " + cg.len_in);
            System.out.println("len_out " + cg.len_out);
            System.out.println("len_arc " + cg.len_arc);
            System.out.println("total_length " + cg.total_length);
            System.out.println("corner_angle " + cg.corner_angle);
            System.out.println("arc_angle " + cg.arc_angle);
            System.out.println("corner_start " + cg.corner_start.x + " " + cg.corner_start.y);
            System.out.println("corner_end " + cg.corner_end.x + " " + cg.corner_end.y);
            System.out.println("arc_centre " + cg.arc_centre.x + " " + cg.arc_centre.y);
        }
    }
}
