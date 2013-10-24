package ch.brotzilla.monalisa.evolution.selectors;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.intf.RangeSelector;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class BasicRangeSelector implements RangeSelector {

    private final int len, min, max;
    
    public BasicRangeSelector(int range) {
        Preconditions.checkArgument(range > 0, "The parameter 'range' has to be greater than zero");
        this.len = range * 2 + 1;
        this.max = range;
        this.min = -range;
        
    }
    
    @Override
    public int getMin() {
        return min;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public int select(MersenneTwister rng) {
        return rng.nextInt(len) - max;
    }

}
