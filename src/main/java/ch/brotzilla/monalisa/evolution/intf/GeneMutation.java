package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public interface GeneMutation extends Mutation {

    public Gene apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Gene input);
    
}
