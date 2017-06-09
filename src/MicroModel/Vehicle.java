package MicroModel;

public class Vehicle {

    // Spatial properties
    public float vehicleLength;
    public SpatialVector position;
    public float speed;
    public SpatialVector acceleration;

    // Occupancy and capacity
    public int occupancy;
    public int capacity;

    // Some random parameters
    private float randomParameterOne   = (float) RandomDrawer.drawFromGaussian(0.5, 0.15);
    private float randomParameterTwo   = (float) RandomDrawer.drawFromGaussian(0.5, 0.15);
    private float randomParameterThree = (float) RandomDrawer.drawFromGaussian(0.5, 0.15);

    // Distance to vehicle ahead
    public float distVehicleAhead;
    // Front to front distance between vehicles standing in queue with calibration parameters
    public float distStanding;
    private float distStandingCalibrationAdd;
    private float distStandingCalibrationMultiply;
    // Minimum following distance
    public float distFollowing;
    private float distFollowingCalibrationAdd;
    private float distFollowingCalibrationMultiply;
    // Point at which driver takes action on noticing that car ahead is slowing
    public float distActionClosing;
    public float distPerceptionClosing;
    private float distClosingCalibrationConstant;
    private float distClosingCalibrationAdd;
    private float distClosingCalibrationMultiply;
    // Point at which driver takes action on noticing that car ahead is accelerating away
    public float distActionOpening;
    public float distPerceptionOpening;
    private float distOpeningCalibrationAdd;
    private float distOpeningCalibrationMultiply;
    private float distOpeningPerceptionCalibrationAdd;
    private float distOpeningPerceptionCalibrationMultiply;
    // Safety distance
    public float distSafety;


    public void computeStandingDistance () {
        /* Computes the standing distance to the car in front
         */
        distStanding = vehicleLength + distStandingCalibrationAdd + randomParameterOne * distStandingCalibrationMultiply;
    }

    public void computeFollowingDistance () {
        /* Computes the minimum following distance from the car in front
         */
        float calibrationValue = distFollowingCalibrationAdd * distFollowingCalibrationMultiply * randomParameterTwo;
        distFollowing = distStanding + calibrationValue * (float) Math.sqrt(speed);
    }

    public void computeClosingDistance () {
        /* Computes the distance at which the driver will take action when closing the gap with the vehicle in front
         */
        float calibrationValue = distClosingCalibrationConstant * (distClosingCalibrationAdd *
                                 distClosingCalibrationMultiply * (randomParameterOne + randomParameterTwo));
        distActionClosing = (float) Math.pow(((distVehicleAhead - distStanding) / calibrationValue), 2f);
        distPerceptionClosing = distActionClosing * (float) Math.pow((double) (distFollowing - distStanding), 2);
    }

    public void computeOpeningDistance () {
        /* Computes the distance at which the driver will take action when closing the gap with the vehicle in front
         */
        float calibrationValue = distOpeningCalibrationAdd + distOpeningCalibrationMultiply *
                                 (randomParameterThree - randomParameterTwo);
        distActionOpening = distStanding + calibrationValue * (distFollowing - distStanding);
        distPerceptionOpening = distPerceptionClosing * (distOpeningPerceptionCalibrationAdd +
                                                         distOpeningPerceptionCalibrationMultiply * randomParameterThree);
    }

    public void setCalibrationParameters () {
        /* Set all the nasty-ass calibration parameters, I haven't found values for these yet so I'll switch them off
        for now.
         */
        distStandingCalibrationAdd = 0;
        distStandingCalibrationMultiply = 1;
        distFollowingCalibrationAdd = 0;
        distFollowingCalibrationMultiply = 1;
        distClosingCalibrationConstant = 1;
        distClosingCalibrationAdd = 0;
        distClosingCalibrationMultiply = 1;
        distOpeningCalibrationAdd = 0;
        distOpeningCalibrationMultiply = 1;
        distOpeningPerceptionCalibrationAdd = 0;
        distOpeningPerceptionCalibrationMultiply = 1;
    }

    public void step () {
        /* Increments the vehicle by one timestep
         */
        computeStandingDistance();
        computeFollowingDistance();
        computeClosingDistance();
        computeOpeningDistance();
    }



}
