package MicroModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    public double vehicleLength = 5.0;  //m
    public double vehicleWidth = 2.0; //m

    // Driving characteristics
    private double maxDeceleration = -9.81; // Max deceleration at 1G
    private double idealVelocity = 35; // ms^-1 velocity the driver 'wants' to travel at
    private double maxVelocity = 80 * 1000 / (60 * 60); // max vehicle velocity is 80 kmh^-1
    private double randomNumber = Math.random(); // a random number to inject variation in driving style


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
                "sdxc", "minimum safe following distance",
                "sdxo", "maximum following distance",
                "sdxv", "",
                "sdv", "distance at which driver notices gap ahead is closing",
                "sdvc", "minimum perceptible closing speed difference",
                "sdvo", "minimum perceptible opening speed difference",
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
                "ation",
                "statusCode"
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

    public void updateAcceleration () {

        double dx = vehicleAhead.position - position - vehicleAhead.vehicleLength;
        double dv = vehicleAhead.velocity - velocity;

        if (vehicleAhead.velocity <= 0) {
            status.put("sdxc", params.get("CC0"));
        } else {
            double vSlow;
            if (dv >= 0 | vehicleAhead.acceleration < -1) {
                vSlow = velocity;
            } else {
                vSlow = vehicleAhead.velocity + dv * (randomNumber - 0.5);
            }
            status.put("sdxc", params.get("CC0") + params.get("CC1") * vSlow);
        }

        status.put("sdxo", status.get("sdxc") + params.get("CC2"));
        status.put("sdxv", status.get("sdxo") + params.get("CC3") * (dv - params.get("CC4")));
        status.put("sdv", params.get("CC6") * dv * dx);

        if (vehicleAhead.velocity > 0) {
            status.put("sdvc", params.get("CC4") - status.get("sdv"));
        } else {
            status.put("sdvc", 0.0);
        }

        if (velocity > params.get("CC5")) {
            status.put("sdvo", status.get("sdv") + params.get("CC5"));
        } else {
            status.put("sdvo", status.get("sdv"));
        }

        double newAcceleration;

        // Analyse the computed statuses and determine the next course of action for the vehicle
        if ((dv < status.get("sdvo")) && (dx <= status.get("sdxc"))) {
            // Vehicle too close to one ahead; decelerate and increase distance
            if (velocity > 0) {
                if (dv < 0) {
                    if (dx > params.get("CC0")) {
                        newAcceleration = Math.min(vehicleAhead.acceleration + dv * dx / (params.get("CC0") - dx), acceleration);
                    } else {
                        newAcceleration = Math.min(vehicleAhead.acceleration + 0.5 * (dv - status.get("sdvo")), acceleration);
                    }
                }
                if (acceleration > -params.get("CC7")) {
                    newAcceleration = -params.get("CC7");
                } else {
                    newAcceleration = Math.max(acceleration, -10 + 0.5 * Math.sqrt(velocity));
                }
            }
        } else if (dv < status.get("sdvc") && (dx < status.get("sdxv"))) {
            // Vehicle is drawing in on a slowing vehicle ahead - e.g. at lights, decelerate and reduce distance
            newAcceleration = Math.max(0.5 * dv * dv / (status.get("sdxc") - dv - 0.1), maxDeceleration);
        } else if (dv < status.get("sdvo") && (dx < status.get("sdxo"))) {
            // Vehicle is doing great! Maintain distance and jus' keep on truckin'!
            if (acceleration <= 0) {
                newAcceleration = Math.min(acceleration, -params.get("CC7"));
            } else {
                newAcceleration = Math.min(Math.max(acceleration, params.get("CC7")), idealVelocity - velocity);
            }
        } else {
            // Vehicle ahead is not constraining current vehicle - accelerate or maintain speed
            if (dx > status.get("sdxc")) {
                double maxAcceleration;
                if (report.get("statusCode") == "c") {
                    newAcceleration = params.get("CC7");
                } else {
                    maxAcceleration = params.get("CC8") + params.get("CC9") * Math.min(velocity, maxVelocity) + randomNumber;
                    if (dx < status.get("sdxo")) {
                        newAcceleration = Math.min(dv * dv / (status.get("sdxo") - dx), maxAcceleration);
                    } else {
                        newAcceleration = maxAcceleration;
                    }
                }
                newAcceleration = Math.min(newAcceleration, idealVelocity - velocity);
                if (Math.abs(idealVelocity - velocity) < 0.1) {
                    // At top speed
                }
            }
        }
    }
}
