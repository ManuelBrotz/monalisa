package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface GenomeMutation extends Mutation {

    public Genome apply(MersenneTwister rng, Context context, EvolutionContext evolutionContext, Genome input);
    
}
