package MicroModel.tests;

import MicroModel.vehicles.Car;
import MicroModel.vehicles.PrototypeVehicle;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import static java.lang.Math.max;

/**
 * Created by daniel on 21/06/17.
 */
public class StraightSimulation {

    double      roadLength;
    Deque<Car>  fleet;
    Car         endOfRoad;

    public StraightSimulation (double roadLength) {
        this.roadLength = roadLength;
        fleet = new LinkedList<>();
        endOfRoad = new Car(roadLength + 5, 0);
    }

    public void leaveSegment() {
        fleet.pollFirst();
        if(!fleet.isEmpty()) fleet.peekFirst().vehicleAhead = endOfRoad;
    }

    public void enterSegment(Car v) {
        v.position = 0.0;
        if(!fleet.isEmpty()) {
            v.vehicleAhead = fleet.peekLast();
        } else {
            v.vehicleAhead = endOfRoad;
        }
        fleet.addLast(v);
    }

    public void step (double dt) {
        if(fleet.isEmpty()) return;
        if(fleet.peek().position >= roadLength) {
            leaveSegment();
        }

        for (Car vehicle: fleet) {
            vehicle.step(dt);
        }
    }

    public static void main (String[] args) {

        // Simulation settings
        double initialRoadLength = 500;
        double dt = 0.1;
        double simDuration = 200;//0.5;
        Random rand = new Random();

        // Setup a new simulation
        StraightSimulation sim = new StraightSimulation(initialRoadLength);

        // Run simulation
        for(int i=0; i<simDuration; ++i) {
            if(i==0 || (rand.nextDouble() < 0.75*dt && sim.fleet.peekLast().position > 5.0)) {
                sim.enterSegment(
                        new Car(
                                0.0,
                                max(0.0,14 + rand.nextGaussian() * 5)
                        )
                );
            }
            sim.step(dt);
            for(Car v : sim.fleet) {
//                System.out.println(v.position + " "+ v.velocity+" "+v.acceleration);
                System.out.println(v.position + " "+ v.position+" "+v.acceleration);
            }
            System.out.println();
            System.out.println();
        }
    }

}
