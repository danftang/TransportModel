package MicroModel.tests;

import MicroModel.roads.RoadSegment;
import MicroModel.signs.PrototypeSign;
import MicroModel.utilities.SpatialVector;
import MicroModel.utilities.WriteFile;
import MicroModel.vehicles.PrototypeVehicle;

import java.util.ArrayList;

public abstract class PrototypeSimulation {

    ArrayList<PrototypeVehicle> fleet = new ArrayList<>();
    ArrayList<RoadSegment> roadSegments = new ArrayList<>();
    int iterationNumber = 0;
    WriteFile data;


    public abstract void initialiseRoad ();
    public abstract void initialiseSignage ();
    public abstract void initialiseFleet ();


    private void plotOutput (double dt) {

        Double time = dt * iterationNumber;
        for (PrototypeVehicle vehicle: fleet) {
            SpatialVector pos = vehicle.getLocation();
            data.writeToFile("veh " +
                    Double.toString(time) +" "+
                    Double.toString(pos.x) +" "+
                    Double.toString(pos.y));
        }
        for (RoadSegment road: roadSegments) {
            for (PrototypeSign sign: road.signage) {
                SpatialVector pos = road.convertPositionToLocation(sign.position);
                data.writeToFile("sig " +
                        Double.toString(time) +" "+
                        Double.toString(pos.x) +" "+
                        Double.toString(pos.y) +" "+
                        Integer.toString(sign.status));
            }

        }
        data.writeToFile("\n \n");
    }


    public void step (double dt) {
        /* Step forwards by dt
         */

        // Update the vehicles for the given timestep
        for (PrototypeVehicle vehicle: fleet) {
            vehicle.step(dt);
        }

        // Update the road segments to assign to each vehicle the vehicle-ahead and its dx from it
        for (RoadSegment road: roadSegments) {
            road.step(dt);
        }

        plotOutput(dt);
        iterationNumber += 1;
    }


    public void run (double dt, double simDuration) {
        /* Run the sim
         */

        for (int i=0; i*dt<=simDuration; i++) {
            step(dt);
        }
    }


}
