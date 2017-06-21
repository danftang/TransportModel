package MacroModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by daniel on 08/06/17.
 */
public class Entity implements Collection<Entity> {
    static Random                       rand = new Random();
    Entity                              container;
    private final Collection<Entity>    collection;

    Entity(Entity container, Collection<Entity> collection) {
        this.container = container;
        this.collection = collection;
    }

    Entity(Entity container) {
        this(container, new ArrayList<>());
    }

    void moveTo(Entity e) {
        container.remove(this);
        container = e;
        e.collection.add(this);
    }

    public Entity location() {
        if(container == null) return this;
        return container.location();
    }

    // Delegation for Container interface

    public Iterator<Entity> iterator() {
        return collection.iterator();
    }

    public int size() {
        return collection.size();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return null;
    }

    public boolean add(Entity entity) {
        return collection.add(entity);
    }

    public boolean remove(Object o) {
        return collection.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Entity> collection) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    public void clear() {
        collection.clear();
    }
}
