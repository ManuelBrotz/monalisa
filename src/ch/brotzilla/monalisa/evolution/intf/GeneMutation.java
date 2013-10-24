package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.vectorizer.Context;

public interface GeneMutation extends Mutation {

    public Gene apply(MersenneTwister rng, Context context, EvolutionContext evolutionContext, Gene input);
    
}
