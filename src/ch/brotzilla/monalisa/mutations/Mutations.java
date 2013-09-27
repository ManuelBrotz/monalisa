package ch.brotzilla.monalisa.mutations;

import java.util.LinkedList;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.mutations.intf.GeneSelector;
import ch.brotzilla.monalisa.mutations.intf.GenomeMutation;
import ch.brotzilla.monalisa.mutations.intf.Mutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class Mutations {

    public static final GeneSelector DEFAULT_GENE_SELECTOR = new BasicGeneSelector();
    
    protected GeneSelector selector = DEFAULT_GENE_SELECTOR;
    protected LinkedList<GeneMutation> geneMutations = Lists.newLinkedList();
    protected LinkedList<GenomeMutation> genomeMutations = Lists.newLinkedList();
    
    protected int maxMutations = -1;
    
    public Mutations() {}
    
    public GeneSelector getGeneSelector() {
        return selector;
    }
    
    public Mutations setGeneSelector(GeneSelector value) {
        if (value == null) {
            this.selector = DEFAULT_GENE_SELECTOR;
        } else {
            this.selector = value;
        }
        return this;
    }

    public Iterable<GeneMutation> getGeneMutations() {
        return geneMutations;
    }
    
    public Iterable<GenomeMutation> getGenomeMutations() {
        return genomeMutations;
    }
    
    public int getMaxMutations() {
        return maxMutations;
    }
    
    public Mutations setMaxMutations(int value) {
        Preconditions.checkArgument(value >= -1, "The parameter 'value' has to be greater than or equal to -1");
        this.maxMutations = value;
        return this;
    }
    
    public Mutations add(Mutation mutation) {
        Preconditions.checkNotNull(mutation, "The parameter 'mutation' must not be null");
        if (mutation instanceof GeneMutation) {
            geneMutations.add((GeneMutation) mutation);
        }
        if (mutation instanceof GenomeMutation) {
            genomeMutations.add((GenomeMutation) mutation);
        }
        return this;
    }
    
    public Genome apply(MersenneTwister rng, Constraints constraints, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(constraints, "The parameter 'constraints' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        Preconditions.checkArgument(geneMutations.size() > 0 && genomeMutations.size() > 0);
        Genome result = new Genome(input);
        int count = 0;
        while (count == 0) {
            for (final GeneMutation m : geneMutations) {
                final double p = m.getProbability();
                if (p > 0 && rng.nextBoolean(p)) {
                    final int index = selector.select(rng, result.genes.length);
                    final Gene gene = result.genes[index];
                    final Gene mutated = m.apply(rng, constraints, gene);
                    if (mutated != gene) {
                        result.genes[index] = mutated;
                        count++;
                        if (maxMutations > 0 && count >= maxMutations) {
                            result.mutations = count;
                            return result;
                        }
                    }
                }
            }
            for (final GenomeMutation m : genomeMutations) {
                final double p = m.getProbability();
                if (p > 0 && rng.nextBoolean(p)) {
                    final Genome mutated = m.apply(rng, constraints, result);
                    if (mutated != result) {
                        result = mutated;
                        count++;
                        if (maxMutations > 0 && count >= maxMutations) {
                            result.mutations = count;
                            return result;
                        }
                    }
                }
            }
        }
        result.mutations = count;
        return result;
    }
}

















