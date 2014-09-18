package ch.brotzilla.monalisa.evolution.selectors;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.intf.ObjectSelector;
import ch.brotzilla.util.MersenneTwister;

public class TObjectSelector<T> implements ObjectSelector<T> {

    private final Object[] entries;
    private final int length;
    
    @SuppressWarnings("unchecked")
    private T select(MersenneTwister rng, int index) {
        Object result = entries[index];
        if (result != null && result instanceof ObjectSelector) {
            return ((ObjectSelector<T>) result).select(rng);
        }
        return (T) result;
    }

    private TObjectSelector(Object[] entries) {
        Preconditions.checkNotNull(entries, "The parameter 'entries' must not be null");
        Preconditions.checkArgument(entries.length > 0, "The length of the parameter 'entries' has to be greater than zero");
        this.entries = entries;
        this.length = entries.length;
    }

    @Override
    public T select(MersenneTwister rng) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        if (length == 1) {
            return select(rng, 0);
        }
        return select(rng, rng.nextInt(length));
    }
    
    public static <T> TObjectSelector.Builder<T> newBuilder() {
        return new Builder<T>();
    }

    public static class Builder<T> {

        private final List<Object> entries;

        private Builder() {
            this.entries = Lists.newArrayList();
        }

        public int getLength() {
            return entries.size();
        }
        
        public Object get(int index) {
            return entries.get(index);
        }
        
        public Builder<T> clear() {
            entries.clear();
            return this;
        }
        
        public Builder<T> add(ObjectSelector<T> value) {
            Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
            entries.add(value);
            return this;
        }
        
        public Builder<T> add(@SuppressWarnings("unchecked") ObjectSelector<T>... values) {
            Preconditions.checkNotNull(values, "The parameter 'values' must not be null");
            Preconditions.checkArgument(values.length > 0, "The length of the 'parameter' values has to be greater than zero");
            for (final ObjectSelector<T> value : values) {
                add(value);
            }
            return this;
        }
        
        public Builder<T> add(T value) {
            Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
            entries.add(value);
            return this;
        }
        
        public Builder<T> add(@SuppressWarnings("unchecked") T... values) {
            Preconditions.checkNotNull(values, "The parameter 'values' must not be null");
            Preconditions.checkArgument(values.length > 0, "The length of the 'parameter' values has to be greater than zero");
            for (final T value : values) {
                add(value);
            }
            return this;
        }
        
        public TObjectSelector<T> build() {
            Preconditions.checkState(entries.size() > 0, "The number of entries has to be greater than zero");
            final int length = entries.size();
            final Object[] e = new Object[length];
            for (int i = 0; i < entries.size(); i++) {
                e[i] = entries.get(i);
            }
            return new TObjectSelector<T>(e);
        }
        
    }

}
