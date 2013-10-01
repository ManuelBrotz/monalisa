package ch.brotzilla.monalisa.mutations.intf;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface GeneMutation extends Mutation {

    public Gene apply(MersenneTwister rng, Context context, Gene input);
    
}
