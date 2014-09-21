package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public interface FitnessFunction {
    
    double compute(VectorizerConfig config, Genome genome, int[] inputData);
    
    double compute(VectorizerConfig config, Genome genome);
    
    boolean isImprovement(Genome latest, Genome mutated);
    
    String format(double fitness);
}
