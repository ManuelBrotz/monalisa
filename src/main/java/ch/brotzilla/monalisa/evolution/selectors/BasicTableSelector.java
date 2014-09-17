package ch.brotzilla.monalisa.evolution.selectors;

import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.evolution.intf.TableSelector;
import ch.brotzilla.util.MersenneTwister;

import com.google.common.base.Preconditions;

public class BasicTableSelector<T> implements TableSelector<T> {

    private final IndexSelector selector;
    private final T[] items;
    private final int length;
    
    @SuppressWarnings("unchecked")
    public BasicTableSelector(IndexSelector selector, T... items) {
        Preconditions.checkNotNull(selector, "The parameter 'selector' must not be null");
        Preconditions.checkNotNull(items, "The parameter 'items' must not be null");
        Preconditions.checkArgument(items.length > 0, "The parameter 'items' must not be empty");
        this.selector = selector;
        this.length = items.length;
        this.items = (T[]) new Object[length];
        System.arraycopy(items, 0, this.items, 0, length);
    }

    @SuppressWarnings("unchecked")
    public BasicTableSelector(T... items) {
        this(new BasicIndexSelector(), items);
    }

    public int getLength() {
        return length;
    }
    
    public T get(int index) {
        return items[index];
    }
    
    @Override
    public T select(MersenneTwister rng) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        if (length == 1) {
            return items[0];
        }
        return items[selector.select(rng, length)];
    }
}
