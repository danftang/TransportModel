package MicroModel.vehicles;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by daniel on 22/06/17.
 */
public class Car {
    public Car vehicleAhead;
    Parameters p =  new Parameters();

    // Motion variables
    public double position;
    public double velocity;
    public double acceleration;
    public double length = 2.0; // length of car

    // Wiedemann state
    double dv;      // difference in velocity between this and car in-front
    double dx;      // separation between this and car in-front
    double ax;      // desired front-front distance for standing vehicles
    double abx;     // desired min following distance at low speeds

    // Driving characteristics
    double maxDeceleration = 9.8;   // ms^-2 Max deceleration at 1G
    double vMax;                    // ms^-1 Max vehicle velocity

    public Car (double position, double velocity) {
        Random rand = new Random();
        this.position = position;
        this.velocity = velocity;
        this.acceleration = 0;
        vMax = (80.0 + 20*rand.nextGaussian()) * 1000.0 / (60.0 * 60.0);
    }

    public double updateAcceleration () {
        double sdx;
        double sdv_dx;
        double cldv = -p.CC4;
        double opdv = -p.CC5;

        dv = velocity - vehicleAhead.velocity;
        dx = vehicleAhead.position - position;
        ax = p.CC0 + vehicleAhead.length;
        abx = ax + p.CC1*velocity;//Math.min(velocity, vehicleAhead.velocity);
        sdx = abx + p.CC2; // max desired following dist
        sdv_dx = p.CC3*(dv-p.CC4) + sdx; // max dx able to perceive dv

        if(dx <= abx) {
            return decelerateOpen();
        } else if(dx < sdx) {
            if(dv > cldv) {
                return decelerateClose();
            } else if(dv > opdv) {
                return maintainDistance();
            }
        } else if(dx <= sdv_dx) {
            return decelerateClose();
        }
        return driveFreely();
    }


    public double decelerateOpen () {
        if(dx < ax) return -maxDeceleration;
        return 0.5*dv*dv/(ax - dx) + vehicleAhead.acceleration - maxDeceleration*(abx - dx)/abx;
    }


    public double decelerateClose () {
        return 0.5 * dv * dv / (abx - dx) + vehicleAhead.acceleration;
    }


    public double maintainDistance () {
        return p.CC7 * Math.signum(acceleration);
    }


    public double driveFreely () {
        return p.CC8 - (velocity/vMax)*(p.CC8 - p.CC9);
    }


    public void step (double dt) {
        acceleration = updateAcceleration();
        if(acceleration<0) {
            dt = Math.min(-velocity / acceleration, dt); // in-case we screech to a halt mid-timestep
        }
        position += velocity * dt + 0.5*acceleration*dt*dt;
        velocity += acceleration * dt;
        if(position > vehicleAhead.position - vehicleAhead.length) {
            position = vehicleAhead.position - vehicleAhead.length;
            velocity = 0.0;
            acceleration = 0.0;
        }
    }
}
