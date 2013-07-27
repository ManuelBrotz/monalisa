package ch.brotzilla.monalisa.mutations;

import java.util.LinkedList;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.mutations.intf.GenomeMutation;
import ch.brotzilla.monalisa.mutations.intf.Mutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public final class Mutations {

    protected LinkedList<GeneMutation> geneMutations = Lists.newLinkedList();
    protected LinkedList<GenomeMutation> genomeMutations = Lists.newLinkedList();
    
    public Mutations() {}

    public Iterable<GeneMutation> getGeneMutations() {
        return geneMutations;
    }
    
    public Iterable<GenomeMutation> getGenomeMutations() {
        return genomeMutations;
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
        final int length = input.genes.length;
        while (count == 0) {
            for (final GeneMutation m : geneMutations) {
                final double p = m.getProbability();
                if (p > 0 && rng.nextBoolean(p)) {
                    final int index = rng.nextInt(length);
                    final Gene gene = result.genes[index];
                    final Gene mutated = m.apply(rng, constraints, gene);
                    if (mutated != gene) {
                        result.genes[index] = mutated;
                        count++;
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
                    }
                }
            }
        }
        result.mutations = count;
        return result;
    }
}

















