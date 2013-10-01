package ch.brotzilla.monalisa.mutations.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface MutationStrategy {

    Genome apply(MersenneTwister rng, Context context, Genome input);
    
}
