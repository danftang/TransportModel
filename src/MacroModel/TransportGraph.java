package MacroModel;

import java.util.ArrayList;

/**
 * Created by daniel on 02/06/17.
 */


public class TransportGraph {
    TransportGraph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        people = new ArrayList<>();
        vehicles = new ArrayList<>();
    }

    void step() {
        int i;
        for(Vehicle v : vehicles) {
            v.step();
        }
        for(Person p : people) {
            p.step();
        }
    }

    ArrayList<Node> nodes;
    ArrayList<Edge> edges;
    ArrayList<Person> people;
    ArrayList<Vehicle> vehicles;
}
