package ch.brotzilla.monalisa.evolution.selectors;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.intf.ObjectSelector;
import ch.brotzilla.util.MersenneTwister;

public class SingleObjectSelector<T> implements ObjectSelector<T> {

    private final T value;
    
    public SingleObjectSelector(T value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        this.value = value;
    }

    @Override
    public T select(MersenneTwister rng) {
        return value;
    }

    public static <T> SingleObjectSelector<T> build(T value) {
        return new SingleObjectSelector<T>(value);
    }
}
