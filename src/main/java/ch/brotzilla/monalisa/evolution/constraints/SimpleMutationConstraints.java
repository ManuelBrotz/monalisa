package ch.brotzilla.monalisa.evolution.constraints;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.evolution.intf.GenomeConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public final class SimpleMutationConstraints extends MutationConstraints {

    private final GenomeConstraint genomeConstraint;
    private final GeneConstraint geneConstraint;

    public SimpleMutationConstraints(GenomeConstraint genomeConstraint, GeneConstraint geneConstraint) {
        this.genomeConstraint = genomeConstraint;
        this.geneConstraint = geneConstraint;
    }
    
    public GenomeConstraint getGenomeConstraint() {
        return genomeConstraint;
    }
    
    public GeneConstraint getGeneConstraint() {
        return geneConstraint;
    }

    @Override
    public boolean acceptable(VectorizerConfig config, Genome genome) {
        if (genomeConstraint == null) {
            return true;
        }
        return genomeConstraint.acceptable(config, genome);
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        if (geneConstraint == null) {
            return true;
        }
        return geneConstraint.satisfied(config, gene);
    }

}
