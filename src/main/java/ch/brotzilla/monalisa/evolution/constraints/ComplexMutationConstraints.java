package ch.brotzilla.monalisa.evolution.constraints;

import java.util.List;

import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.evolution.intf.GenomeConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public final class ComplexMutationConstraints extends MutationConstraints {

    private final List<GenomeConstraint> genomeConstraints;
    private final List<GeneConstraint> geneConstraints;

    public ComplexMutationConstraints() {
        this.genomeConstraints = Lists.newArrayList();
        this.geneConstraints = Lists.newArrayList();
    }
    
    public List<GenomeConstraint> getGenomeConstraints() {
        return genomeConstraints;
    }
    
    public List<GeneConstraint> getGeneConstraints() {
        return geneConstraints;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Genome genome) {
        if (genomeConstraints.isEmpty()) {
            return true;
        }
        for (final GenomeConstraint c : genomeConstraints) {
            if (c != null & !c.satisfied(config, genome)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        if (geneConstraints.isEmpty()) {
            return true;
        }
        for (final GeneConstraint c : geneConstraints) {
            if (c != null && !c.satisfied(config, gene)) {
                return false;
            }
        }
        return true;
    }

}
