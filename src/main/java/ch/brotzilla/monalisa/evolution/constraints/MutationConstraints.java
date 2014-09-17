package ch.brotzilla.monalisa.evolution.constraints;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.evolution.intf.GenomeConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class MutationConstraints implements GeneConstraint, GenomeConstraint {

    public MutationConstraints() {
    }
    
    @Override
    public boolean satisfied(VectorizerConfig config, Genome genome) {
        return true;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        return true;
    }

}
