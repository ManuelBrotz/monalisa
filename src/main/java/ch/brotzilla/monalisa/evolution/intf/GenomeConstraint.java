package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public interface GenomeConstraint {
    
    boolean satisfied(VectorizerConfig config, Genome genome);
    
}
