package MicroModel.signs;


import java.util.HashMap;

// TODO prolly want 'position' to be submittable as a location vector?


public class PelicanCrossing extends PrototypeSign {

    double greenTime;
    double amberTime;
    double redTime;

    double timeCounter = 0;
    double totalCycleTime;

    HashMap<String, Integer> statusMap = new HashMap<>();


    public PelicanCrossing (double position, double greenTime, double amberTime, double redTime, boolean initialiseRed) {

        this.position = position;
        this.greenTime = greenTime;
        this.amberTime = amberTime;
        this.redTime = redTime;

        this.totalCycleTime = greenTime + amberTime + redTime + amberTime;

        statusMap.put("green", 0);
        statusMap.put("amber", 1);
        statusMap.put("red", 2);
        statusMap.put("flashing amber", 3);

        if (initialiseRed) {
            stop = true;
            timeCounter = greenTime + amberTime;
            status = statusMap.get("red");
        } else {
            stop = false;
            status = statusMap.get("green");
        }
    }


    public void step (double dt) {
        /*
         */
        // Increment the counter - restart the cycle if necessary
        timeCounter += dt;
        if (timeCounter > totalCycleTime) {timeCounter -= totalCycleTime;}

        // Check the status
        if (timeCounter <= greenTime) {
            status = statusMap.get("green");
        } else if (timeCounter <= greenTime + amberTime) {
            status = statusMap.get("amber");
        } else if (timeCounter <= greenTime + amberTime + redTime) {
            status = statusMap.get("red");
        } else {
            status = statusMap.get("flashing amber");
        }

        // Set the status
        if (status == 1 | status == 2) {
            stop = true;
        } else {
            stop = false;
        }




    }
}
