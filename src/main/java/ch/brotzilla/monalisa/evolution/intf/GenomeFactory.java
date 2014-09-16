package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public interface GenomeFactory {

    Gene createGene(MersenneTwister rng, VectorizerConfig config);
    
    Genome createGenome(MersenneTwister rng, VectorizerConfig config);
    
}
