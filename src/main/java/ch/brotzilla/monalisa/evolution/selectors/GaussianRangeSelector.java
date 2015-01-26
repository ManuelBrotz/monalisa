package ch.brotzilla.monalisa.evolution.selectors;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.intf.RangeSelector;
import ch.brotzilla.util.MersenneTwister;

public class GaussianRangeSelector implements RangeSelector {

    private final int deviation, min, max;
    private final boolean canReturnZero;
    
    public GaussianRangeSelector(int deviation, boolean canReturnZero) {
        Preconditions.checkArgument(deviation > 0, "The parameter 'deviation' has to be greater than zero");
        this.deviation = deviation;
        this.min = deviation * -3;
        this.max = deviation * 3;
        this.canReturnZero = canReturnZero;
    }
    
    public int getDeviation() {
        return deviation;
    }
    
    public boolean canReturnZero() {
        return canReturnZero;
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
        final double gaussian = rng.nextGaussian();
        int value = (int) Math.round(gaussian * deviation);
        if ((value == 0 && !canReturnZero) || value < min || value > max) {
            return select(rng);
        }
        return value;
    }

}
