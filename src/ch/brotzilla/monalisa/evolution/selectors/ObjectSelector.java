package ch.brotzilla.monalisa.evolution.selectors;

import ch.brotzilla.monalisa.utils.MersenneTwister;

import com.google.common.base.Preconditions;

public class ObjectSelector<T> {

    private final T[] items;
    private final int length;
    
    public ObjectSelector(@SuppressWarnings("unchecked") T... items) {
        Preconditions.checkNotNull(items, "The parameter 'items' must not be null");
        Preconditions.checkArgument(items.length > 0, "The parameter 'items' must not be empty");
        this.items = items;
        this.length = items.length;
    }
    
    public int getLength() {
        return length;
    }
    
    public T select(MersenneTwister rng) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        if (length == 1) {
            return items[0];
        }
        return items[rng.nextInt(length)];
    }
}
