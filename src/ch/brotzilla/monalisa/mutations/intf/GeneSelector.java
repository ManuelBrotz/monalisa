package ch.brotzilla.monalisa.mutations.intf;

import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface GeneSelector {

    int select(MersenneTwister rng, int length);
    
}
