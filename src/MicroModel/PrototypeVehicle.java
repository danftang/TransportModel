package MicroModel;

import java.util.*;

// TODO need to update the car in front at each timestep...

public class PrototypeVehicle {

    // Proximate vehicles
    public PrototypeVehicle vehicleAhead;

    // Motion status
    public HashMap<String, Double> status;
    public HashMap<String, String> statusDescription;

    // Motion variables
    public double position;
    public double velocity;
    public double acceleration;

    // Maps to hold driving parameters
    public HashMap<String, Double> params;
    public HashMap<String, String> paramDescription;
    public HashMap<String, String> paramUnits;

    // Current report of vehicle driving condition
    public HashMap<String, String> report;
    public HashMap<String, String> newReport;

    // Vehicle properties
    public String identification;
    public double vehicleLength = 5.0;                  // m
    public double vehicleWidth = 2.0;                   // m

    // Driving characteristics
    private double maxDeceleration = -9.81;             // ms^-2 Max deceleration at 1G
    private double idealVelocity = 35;                  // ms^-1 Velocity the driver 'wants' to travel at
    private double maxVelocity = 80 * 1000 / (60 * 60); // ms^-1 Max vehicle velocity is 80 kmh^-1
    private double randomNumber = Math.random() / 2;        // a random number to inject variation in driving style

    // TODO hack zone
    public double circum;


    public PrototypeVehicle (double position, double velocity, String identification) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = 0;
        this.identification = identification;

        // Initialise the parameters with the default Weidemann params.
        setDefaultParams();

    }

    public PrototypeVehicle (double position, double velocity) {

        this.position = position;
        this.velocity = velocity;
        this.acceleration = 0;

        // Set a random identification
        this.identification = UUID.randomUUID().toString().substring(0, 5);

        // Initialise the parameters with the default Weidemann params.
        setDefaultParams();
    }


    public void initialiseParams () {

        List<String> parameters = new ArrayList<>(Arrays.asList(
                "CC0", "Standstill distance", "m",
                "CC1", "Spacing time", "s",
                "CC2", "Following variation", "m",
                "CC3", "Threshold to follow", "s",
                "CC4", "Negative following threshold", "ms^-1",
                "CC5", "Positive following threshold", "ms^-1",
                "CC6", "Speed dependency of oscillation", "rads^-1",
                "CC7", "Oscillation acceleration", "ms^-2",
                "CC8", "Standstill acceleration", "ms^-2",
                "CC9", "Acceleration at 80 kmh^-1", "ms^-2"
        ));

        paramDescription = new HashMap<>();
        paramUnits = new HashMap<>();
        params = new HashMap<>();

        for (int i = 0; i < parameters.size(); i += 3) {
            paramDescription.put(parameters.get(i), parameters.get(i + 1));
            paramUnits.put(parameters.get(i), parameters.get(i + 2));
            params.put(parameters.get(i), 0.0);
        }

        List<String> variables = new ArrayList<>(Arrays.asList(
                "dx", "distance to vehicle ahead",
                "dv", "velocity difference to vehicle ahead",
                "sdxc", "minimum safe following distance",
                "sdxo", "maximum following distance",
                "sdxv", "",
                "sdv", "distance at which driver notices gap ahead is closing",
                "sdvc", "minimum perceptible closing speed difference",
                "sdvo", "minimum perceptible opening speed difference"
        ));

        status = new HashMap<>();
        statusDescription = new HashMap<>();

        for (int i=0; i<variables.size(); i+=2) {
            status.put(variables.get(i), 0.0);
            statusDescription.put(variables.get(i), variables.get(i+1));
        }

        List<String> reportHeadings = new ArrayList<>(Arrays.asList(
                "status",
                "condition",
                "action"
        ));

        report = new HashMap<>();
        newReport = new HashMap<>();

        for (int i=0; i<reportHeadings.size(); i++) {
            report.put(reportHeadings.get(i), "");
            newReport.put(reportHeadings.get(i), "");
        }

    }

    public void setOriginalParams () {
        initialiseParams();
        params.put("CC0", 1.5);
        params.put("CC1", 1.3);
        params.put("CC2", 4.0);
        params.put("CC3", -12.0);
        params.put("CC4", -0.25);
        params.put("CC5", 0.35);
        params.put("CC6", 6.0);
        params.put("CC7", 0.25);
        params.put("CC8", 2.0);
        params.put("CC9", 1.5);
    }

    public void setDefaultParams () {
        setOriginalParams();
        params.put("CC0", 1.35);
        params.put("CC1", 1.17);
        params.put("CC2", 8.0);
        params.put("CC4", -1.5);
        params.put("CC5", 2.1);
    }

    public double updateAcceleration () {

        status.put("dx", vehicleAhead.position - position - vehicleAhead.vehicleLength);
        if (vehicleAhead.position < position) {
            // Hateful little hack for circular track...
            status.put("dx", status.get("dx") + circum);
        }

        status.put("dv", vehicleAhead.velocity - velocity);

        if (vehicleAhead.velocity <= 0) {
            // Vehicle ahead is stopped so following distance = stopping distance
            status.put("sdxc", params.get("CC0"));
        } else {
            double vSlow;
            if (status.get("dv") >= 0 | vehicleAhead.acceleration < 0) {
                // Vehicle ahead is going slower than this one
                vSlow = velocity;
            } else {
                // Vehicle ahead is going faster than this one
                vSlow = vehicleAhead.velocity + status.get("dv") * (randomNumber - 0.5);
            }
            // Following distance is stopping distance + following time * velocity from above
            status.put("sdxc", params.get("CC0") + params.get("CC1") * vSlow);
        }

        // Compute remaining status variables
        status.put("sdxo", status.get("sdxc") + params.get("CC2"));
        status.put("sdxv", status.get("sdxo") + params.get("CC3") * (status.get("dv") - params.get("CC4")));
        status.put("sdv", params.get("CC6") * status.get("dx") * status.get("dx"));

        if (vehicleAhead.velocity > 0) {
            // If the vehicle ahead is moving
            status.put("sdvc", params.get("CC4") - status.get("sdv"));
        } else {
            // ...or is stopped...
            status.put("sdvc", 0.0);
        }

        if (velocity > params.get("CC5")) {
            // If velocity is greater than the following threshold
            status.put("sdvo", status.get("sdv") + params.get("CC5"));
        } else {
            status.put("sdvo", status.get("sdv"));
        }

        double newAcceleration = 0.0;

        // Analyse the computed statuses and determine the next course of action for the vehicle
        if ((status.get("dv") < status.get("sdvo")) && (status.get("dx") <= status.get("sdxc"))) {
            // Vehicle too close to one ahead; decelerate and open gap.
            newReport.put("status", "A");
            newAcceleration = decelerateOpen(newAcceleration);
        } else if (status.get("dv") < status.get("sdvc") && (status.get("dx") < status.get("sdxv"))) {
            // Vehicle is drawing in on a slowing vehicle ahead - e.g. at lights, decelerate and reduce distance
            newReport.put("status", "B");
            newAcceleration = decelerateClose(newAcceleration);
        } else if (status.get("dv") < status.get("sdvo") && (status.get("dx") < status.get("sdxo"))) {
            // Maintain distance
            newReport.put("status", "C");
            newAcceleration = maintainDistance(newAcceleration);
        } else {
            // Drive freely - unconstrained by vehicle ahead
            newReport.put("status", "D");
            newAcceleration = driveFreely(newAcceleration);
        }

        return newAcceleration;
    }


    public double decelerateOpen (double newAcceleration) {
        /* Vehicle too close to one ahead; decelerate and increase distance
         */
        // Set the status
        newReport.put("condition", "too close");
        newReport.put("action", "open gap");

        // Compute the new acceleration
        newAcceleration = 0;
        if (velocity > 0) {
            if (status.get("dv") < 0) {
                if (status.get("dx") > params.get("CC0")) {
                    newAcceleration = Math.min(vehicleAhead.acceleration + status.get("dv") * status.get("dv") /
                                      (params.get("CC0") - status.get("dx")), acceleration);
                } else {
                    newAcceleration = Math.min(vehicleAhead.acceleration + 0.5 * (status.get("dv") - status.get("sdvo")),
                                               acceleration);
                }
            }
            if (newAcceleration > -params.get("CC7")) {
                newAcceleration = -params.get("CC7");
            } else {
                newAcceleration = Math.min(newAcceleration, -10 + 0.5 * Math.sqrt(velocity));
            }
        }
        return newAcceleration;
    }


    public double decelerateClose (double newAcceleration) {
        /* Vehicle is drawing in on a slowing vehicle ahead - e.g. at lights, decelerate and reduce distance
         */
        // Set the status
        newReport.put("condition", "too close");
        newReport.put("action", "close gap");

        // Compute the new acceleration
        newAcceleration = Math.max(0.5 * status.get("dv") * status.get("dv") /
                                   (status.get("sdxc") - status.get("dx") - 0.1), maxDeceleration);
        return newAcceleration;
    }


    public double maintainDistance (double newAcceleration) {
        /* Vehicle is doing great! Maintain distance and jus' keep on truckin'!
         */
        // Set the status
        newReport.put("condition", "good distance");
        newReport.put("action", "maintain gap");

        // Compute the new acceleration
        if (acceleration <= 0) {
            newAcceleration = Math.min(acceleration, -params.get("CC7"));
        } else {
            newAcceleration = Math.max(acceleration, params.get("CC7"));
            newAcceleration = Math.min(newAcceleration, idealVelocity - velocity);
        }
        return newAcceleration;
    }


    public double driveFreely (double newAcceleration) {
        /* Vehicle ahead is not constraining current vehicle - accelerate or maintain speed
         */
        // Set the status
        newReport.put("condition", "under no constraint");
        newReport.put("action", "drive free");

        // Compute the new acceleration
        if (status.get("dx") > status.get("sdxc")) {
            double maxAcceleration;
            if (report.get("status") == "D") {
                newAcceleration = params.get("CC7");
            } else {
                maxAcceleration = params.get("CC8") + params.get("CC9") * Math.min(velocity, maxVelocity) + randomNumber;
                if (status.get("dx") < status.get("sdxo")) {
                    newAcceleration = Math.min(status.get("dv") * status.get("dv") / (status.get("sdxo") - status.get("dx")), maxAcceleration);
                } else {
                    newAcceleration = maxAcceleration;
                }
            }
            newAcceleration = Math.min(newAcceleration, idealVelocity - velocity);
            if (Math.abs(idealVelocity - velocity) < 0.1) {
                // At top speed
                newReport.put("condition", "at top speed");
            }
        }
        return newAcceleration;
    }

    public void step (double dt) {
        /* Step the vehicle by one timestep
         */
        // Update velocity
        acceleration = updateAcceleration();
        velocity += acceleration * dt;
        velocity = Math.max(velocity, 0);
        position += velocity * dt;
        // Switch params at t+1 to t
        report = newReport;
    }
}
