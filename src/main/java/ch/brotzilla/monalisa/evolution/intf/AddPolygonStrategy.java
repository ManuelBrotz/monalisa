package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public interface AddPolygonStrategy {

    Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input);
    
}
