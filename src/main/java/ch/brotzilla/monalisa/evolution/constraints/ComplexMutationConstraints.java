package ch.brotzilla.monalisa.evolution.constraints;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.evolution.intf.GenomeConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class ComplexMutationConstraints extends MutationConstraints {

    private final GenomeConstraint[] genomeConstraints;
    private final GeneConstraint[] geneConstraints;

    private ComplexMutationConstraints(Builder builder) {
        Preconditions.checkNotNull(builder, "The parameter 'builder' must not be null");
        builder.checkReady();
        this.genomeConstraints = builder.buildGenomeConstraints();
        this.geneConstraints = builder.buildGeneConstraints();
    }
    
    @Override
    public boolean satisfied(VectorizerConfig config, Genome genome) {
        if (genomeConstraints != null) {
            for (final GenomeConstraint c : genomeConstraints) {
                if (c != null & !c.satisfied(config, genome)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        if (geneConstraints != null) {
            for (final GeneConstraint c : geneConstraints) {
                if (c != null && !c.satisfied(config, gene)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static class Builder implements ch.brotzilla.monalisa.intf.Builder<ComplexMutationConstraints> {

        private final List<GenomeConstraint> genomeConstraints;
        private final List<GeneConstraint> geneConstraints;

        private GenomeConstraint[] buildGenomeConstraints() {
            if (genomeConstraints.size() == 0) {
                return null;
            }
            final GenomeConstraint[] result = new GenomeConstraint[genomeConstraints.size()];
            for (int i = 0; i < genomeConstraints.size(); i++) {
                result[i] = genomeConstraints.get(i);
            }
            return result;
        }
        
        private GeneConstraint[] buildGeneConstraints() {
            if (geneConstraints.size() == 0) {
                return null;
            }
            final GeneConstraint[] result = new GeneConstraint[geneConstraints.size()];
            for (int i = 0; i < geneConstraints.size(); i++) {
                result[i] = geneConstraints.get(i);
            }
            return result;
            
        }
        
        public Builder() {
            this.genomeConstraints = Lists.newArrayList();
            this.geneConstraints = Lists.newArrayList();
        }

        @Override
        public Builder checkReady() {
            Preconditions.checkState(genomeConstraints.indexOf(null) == -1, "The list of genome constraints must not contain null elements");
            Preconditions.checkState(geneConstraints.indexOf(null) == -1, "The list of gene constraints must not contain null elements");
            return this;
        }

        @Override
        public boolean isReady() {
            return genomeConstraints.indexOf(null) == -1
                    && geneConstraints.indexOf(null) == -1;
        }

        public List<GenomeConstraint> getGenomeConstraints() {
            return genomeConstraints;
        }
        
        public List<GeneConstraint> getGeneConstraints() {
            return geneConstraints;
        }
        
        public Builder add(GenomeConstraint constraint) {
            Preconditions.checkNotNull(constraint, "The parameter 'constraint' must not be null");
            genomeConstraints.add(constraint);
            return this;
        }

        public Builder add(GeneConstraint constraint) {
            Preconditions.checkNotNull(constraint, "The parameter 'constraint' must not be null");
            geneConstraints.add(constraint);
            return this;
        }

        @Override
        public ComplexMutationConstraints build() {
            return new ComplexMutationConstraints(this);
        }
        
    }

}
