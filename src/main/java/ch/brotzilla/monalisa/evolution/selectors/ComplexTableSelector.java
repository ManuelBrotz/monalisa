package ch.brotzilla.monalisa.evolution.selectors;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.intf.TableSelector;
import ch.brotzilla.util.MersenneTwister;

public class ComplexTableSelector<T> implements TableSelector<T> {

    private final Entry<T>[] entries;
    private final int length;
    
    private ComplexTableSelector(Entry<T>[] entries) {
        Preconditions.checkNotNull(entries, "The parameter 'entries' must not be null");
        Preconditions.checkArgument(entries.length > 0, "The length of the parameter 'entries' has to be greater than zero");
        this.entries = entries;
        this.length = entries.length;
    }
    
    public int getLength() {
        return length;
    }
    
    public Entry<T> get(int index) {
        return entries[index];
    }
    
    @Override
    public T select(MersenneTwister rng) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        if (length == 1) {
            return entries[0].selector.select(rng);
        }
        final double selector = rng.nextDouble();
        double lower = 0.0d;
        for (int i = 0; i < length; i++) {
            final Entry<T> e = entries[i];
            final double upper = lower + e.probability;
            if (selector > lower && selector <= upper) {
                return e.selector.select(rng);
            }
            lower = upper;
        }
        return entries[length - 1].selector.select(rng);
    }
    
    public static <T> ComplexTableSelector.Builder<T> newBuilder() {
        return new Builder<T>();
    }

    public static class Entry<T> {
        
        private final TableSelector<T> selector;
        private final double probability;
        
        public Entry(TableSelector<T> selector, double probability) {
            Preconditions.checkNotNull(selector, "The parameter 'selector' must not be null");
            Preconditions.checkArgument(probability > 0, "The parameter 'probability' has to be greater than zero");
            this.selector = selector;
            this.probability = probability;
        }
        
        public TableSelector<T> getSelector() {
            return selector;
        }
        
        public double getProbability() {
            return probability;
        }
    }
    
    public static class Builder<T> {

        private final List<Entry<T>> entries;

        private double sum() {
            double sum = 0;
            for (final Entry<T> e : entries) {
                Preconditions.checkNotNull(e, "Null entries are not allowed");
                sum += e.probability;
            }
            return sum;
        }
        
        private Builder() {
            this.entries = Lists.newArrayList();
        }

        public List<Entry<T>> getEntries() {
            return entries;
        }
        
        public Builder<T> add(TableSelector<T> selector, double probability) {
            entries.add(new Entry<T>(selector, probability));
            return this;
        }
        
        @SuppressWarnings("unchecked")
        public ComplexTableSelector<T> build() {
            Preconditions.checkState(entries.size() > 0, "The number of entries has to be greater than zero");
            final double sum = sum();
            final List<Entry<T>> newList = Lists.newArrayList();
            for (final Entry<T> e : entries) {
                final double p = e.probability / sum;
                newList.add(new Entry<T>(e.selector, p));
            }
            return new ComplexTableSelector<T>((Entry<T>[]) newList.toArray());
        }
        
    }

}
