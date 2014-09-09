package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public interface GenomeFactory {

    Gene createGene(MersenneTwister rng, VectorizerContext vc, EvolutionContext ec);
    
    Genome createGenome(MersenneTwister rng, VectorizerContext vc, EvolutionContext ec);
    
}
