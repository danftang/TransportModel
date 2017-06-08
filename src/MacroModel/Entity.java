package MacroModel;

import java.util.ArrayList;

/**
 * Created by daniel on 08/06/17.
 */
public class Entity extends ArrayList<Entity> {

    Entity(Entity container) {
        this.container = container;
    }

    void moveTo(Entity e) {
        container.remove(this);
        container = e;
        e.add(this);
    }

    Entity              container;
}
