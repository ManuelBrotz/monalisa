package ch.brotzilla.monalisa.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.mutations.intf.IndexSelector;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class BiasedIndexSelector implements IndexSelector {

    protected final int bias;
    
    public BiasedIndexSelector(int bias) {
        Preconditions.checkArgument(bias > 2, "The parameter 'bias' has to be greater than 2");
        this.bias = bias;
    }
    
    public int getBias() {
        return bias;
    }

    @Override
    public int select(MersenneTwister rng, int length) {
        final int top = length / bias;
        if (top == 0) {
            return rng.nextInt(length);
        }
        final int bottom = length - top;
        if (rng.nextInt(bias) == 0) {
            return rng.nextInt(bottom);
        }
        return bottom + rng.nextInt(top);
    }

}
