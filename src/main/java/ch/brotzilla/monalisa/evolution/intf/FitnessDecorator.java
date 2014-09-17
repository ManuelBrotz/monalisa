package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public interface FitnessDecorator {
    
    double apply(VectorizerConfig config, Genome genome, double fitness);
    
}
