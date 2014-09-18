package ch.brotzilla.monalisa.evolution.selectors;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.intf.ObjectSelector;
import ch.brotzilla.util.MersenneTwister;

public class PObjectSelector<T> implements ObjectSelector<T> {

    private final Object[] entries;
    private final double[] probabilities;
    private final int length;
    
    @SuppressWarnings("unchecked")
    private T select(MersenneTwister rng, int index) {
        Object result = entries[index];
        if (result != null && result instanceof ObjectSelector) {
            return ((ObjectSelector<T>) result).select(rng);
        }
        return (T) result;
    }

    private PObjectSelector(Object[] entries, double[] probabilities) {
        Preconditions.checkNotNull(entries, "The parameter 'entries' must not be null");
        Preconditions.checkNotNull(probabilities, "The parameter 'probabilities' must not be null");
        Preconditions.checkArgument(entries.length > 0, "The length of the parameter 'entries' has to be greater than zero");
        Preconditions.checkArgument(entries.length == probabilities.length, "The length of the parameters 'entries' and 'probabilities' have to be equal");
        this.entries = entries;
        this.probabilities = probabilities;
        this.length = entries.length;
    }

    @Override
    public T select(MersenneTwister rng) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        if (length == 1) {
            return select(rng, 0);
        }
        final double selector = rng.nextDouble();
        double lower = 0.0d;
        for (int i = 0; i < length; i++) {
            final double upper = lower + probabilities[i];
            if (selector > lower && selector <= upper) {
                return select(rng, i);
            }
            lower = upper;
        }
        return select(rng, length - 1);
    }
    
    public static <T> PObjectSelector.Builder<T> newBuilder() {
        return new Builder<T>();
    }

    public static class Builder<T> {

        private final List<Object> entries;
        private final List<Double> probabilities;

        private double sum() {
            double sum = 0;
            for (final Double p : probabilities) {
                Preconditions.checkNotNull(p, "Null entries are not allowed");
                sum += p;
            }
            return sum;
        }
        
        private Builder() {
            this.entries = Lists.newArrayList();
            this.probabilities = Lists.newArrayList();
        }

        public int getLength() {
            return entries.size();
        }
        
        public Object get(int index) {
            return entries.get(index);
        }
        
        public double getProbability(int index) {
            return probabilities.get(index);
        }
        
        public Builder<T> clear() {
            entries.clear();
            probabilities.clear();
            return this;
        }
        
        public Builder<T> add(ObjectSelector<T> value, double probability) {
            Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
            Preconditions.checkArgument(probability > 0.0d, "The parameter 'probability' has to be greater than zero");
            entries.add(value);
            probabilities.add(probability);
            return this;
        }
        
        public Builder<T> add(T value, double probability) {
            Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
            Preconditions.checkArgument(probability > 0.0d, "The parameter 'probability' has to be greater than zero");
            entries.add(value);
            probabilities.add(probability);
            return this;
        }
        
        public PObjectSelector<T> build() {
            Preconditions.checkState(entries.size() > 0, "The number of entries has to be greater than zero");
            final double sum = sum();
            final int length = entries.size();
            final double[] p = new double[length];
            final Object[] e = new Object[length];
            for (int i = 0; i < probabilities.size(); i++) {
                p[i] = probabilities.get(i) / sum;
                e[i] = entries.get(i);
            }
            return new PObjectSelector<T>(e, p);
        }
        
    }

}
