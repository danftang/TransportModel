package MicroModel;

public class SpatialVector {

    double x;
    double y;
    double z;


    public SpatialVector(double x, double y, double z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }


    public String toString() {
    /* Custom toString method to make it easy to print a String representation of the vector.
     */
        return "NOT WORKING..."; //String.format("%s, %s, %s", x, y, z);
    }


    public SpatialVector add(SpatialVector otherVector) {
    /* Vector addition
     */
        return new SpatialVector(x + otherVector.x, y + otherVector.y, z + otherVector.z);
    }


    public SpatialVector sub(SpatialVector otherVector) {
    /* Vector subtraction
     */
        return new SpatialVector(x - otherVector.x, y - otherVector.y, z - otherVector.z);
    }


    public SpatialVector scale(double scalar) {
    /* Scale the vector (by a double)
     */
        return new SpatialVector(x * scalar, y * scalar, z * scalar);
    }

    public double lengthSquared() {
    /* Returns the squared length of the vector.
    */
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }


    public double length() {
    /* Returns the length of the vector.
     */
        return Math.sqrt(lengthSquared());
    }


    public SpatialVector normalize() {
    /* Get the unit vector
     */
        return scale(1.0 / length());
    }


    public double distanceSquared(SpatialVector otherVector) {
    /* Returns the squared length of the distance between two locations
     */
        SpatialVector differenceVector = this.sub(otherVector);
        return differenceVector.x * differenceVector.x
                + differenceVector.y * differenceVector.y
                + differenceVector.z * differenceVector.z;
    }


    public double distance(SpatialVector otherVector) {
    /* Returns the distance between two points.
    */
        return Math.sqrt(distanceSquared(otherVector));
    }


    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + Double.doubleToLongBits(x);
        bits = 31L * bits + Double.doubleToLongBits(y);
        bits = 31L * bits + Double.doubleToLongBits(z);
        return (int) (bits ^ (bits >> 32));
    }


    public boolean equals(Object object) {
    /* Get expected behaviour from thisVector.equals(sameValuedVector)
     */
        if (object == null) return false;
        if (this == object) return true;
        if (!(object instanceof SpatialVector)) return false;
        SpatialVector other = (SpatialVector)object;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }


}
