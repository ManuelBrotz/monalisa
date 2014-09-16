package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public interface GeneConstraint {
    
    boolean acceptable(VectorizerConfig config, Gene gene);
    
}
