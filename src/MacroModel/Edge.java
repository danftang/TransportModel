package MacroModel;

/**
 * Created by daniel on 02/06/17.
 */
public class Edge extends Entity {

    Edge(Node src, Node dest) {
        super(null);
        this.src = src;
        this.dest = dest;
    }

    Node src;
    Node dest;
    double price;
}
