package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface GenomeMutation extends Mutation {

    public Genome apply(MersenneTwister rng, IndexSelector selector, Context context, Genome input);
    
}
