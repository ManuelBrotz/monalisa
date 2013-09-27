package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.mutations.intf.GeneSelector;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class BasicGeneSelector implements GeneSelector {

    public BasicGeneSelector() {}

    @Override
    public int select(MersenneTwister rng, int length) {
        return rng.nextInt(length);
    }

}
