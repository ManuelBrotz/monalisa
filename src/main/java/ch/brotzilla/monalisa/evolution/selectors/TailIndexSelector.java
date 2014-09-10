package ch.brotzilla.monalisa.evolution.selectors;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.util.MersenneTwister;

public class TailIndexSelector implements IndexSelector {

    protected final int tailSize;
    
    public TailIndexSelector(int tailSize) {
        Preconditions.checkArgument(tailSize > 0, "The parameter 'tailSize' has to be greater than zero");
        this.tailSize = tailSize;
    }

    @Override
    public int select(MersenneTwister rng, int length) {
        return rng.nextInt(length - Math.max(length - tailSize, 0));
    }

}
