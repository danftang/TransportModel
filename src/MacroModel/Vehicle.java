package MacroModel;

/**
 * Created by daniel on 02/06/17.
 */
public class Vehicle extends Entity {

    Vehicle() {
        distanceAlongEdge = 0.0;
        routeLeg = 0;
    }

    void step() {
        if(distanceAlongEdge <= 0.99999) {
            distanceAlongEdge += speed;
        } else {
            Edge currentEdge = route[routeLeg];
            routeLeg += 1;
            if(routeLeg >= route.length) {
                routeLeg = 0;
            }
            distanceAlongEdge = 0.0;
            currentEdge.dest.transfer(this, currentEdge, route[routeLeg]);
        }
    }

    @Override
    public String toString() {
        return(routeLeg+" "+distanceAlongEdge);
    }

    double              distanceAlongEdge;
    int                 routeLeg;
    double              speed;
    int                 localTime;
    Edge []             route;
}
