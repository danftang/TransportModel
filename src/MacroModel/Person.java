package MacroModel;

/**
 * Created by daniel on 02/06/17.
 */
public class Person extends Entity implements Steppable {

    Person(Entity location) {
        super(location);
    }

    public void step() {
        Entity myLocation = location();
        if(isInVehicle()) {
            if(myLocation instanceof Node) {
                if(rand.nextDouble() < 0.2) {
                    moveTo(myLocation);
                }
            }
        } else {
            if(rand.nextDouble() < 0.25 && myLocation.size() > 0) {
                moveTo(myLocation.iterator().next());
            }
        }
    }

    public boolean isInVehicle() {
        return container instanceof Vehicle;
    }



    public double   wealth;
}
