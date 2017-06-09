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

    // Vehicle properties
    public double vehicleLength = 5.0;  //m
    public double vehicleWidth = 2.0; //m


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

        for (int i=0; i<variables.size(); i+=1) {
            status.put(variables.get(i), 0.0);
            statusDescription.put(variables.get(i), variables.get(i+1));
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
                vSlow = vehicleAhead.velocity + dv * (rand - 0.5);
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
        String newStatus;
        String newCondition;
        String newAction;
        String newStatusCode;





    }
}
