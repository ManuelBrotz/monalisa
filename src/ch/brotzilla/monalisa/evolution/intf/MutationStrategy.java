package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;

public interface MutationStrategy {

    Genome apply(MersenneTwister rng, VectorizerContext context, EvolutionContext evolutionContext, Genome input);
    
}
