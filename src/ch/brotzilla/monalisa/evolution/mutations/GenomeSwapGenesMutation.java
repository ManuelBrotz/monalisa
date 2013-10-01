package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.mutations.intf.IndexSelector;
import ch.brotzilla.monalisa.mutations.intf.GenomeMutation;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GenomeSwapGenesMutation extends BasicMutation implements GenomeMutation {

    private int selectSecondIndex(MersenneTwister rng, IndexSelector selector, int firstIndex, int length) {
        int result = firstIndex;
        while (result == firstIndex) {
            result = selector.select(rng, length);
        }
        return result;
    }
    
    public GenomeSwapGenesMutation() {
        super("swap-genes", "Swap Genes", "Swaps two randomly chosen genes of the passed genome");
    }

    @Override
    public Genome apply(MersenneTwister rng, IndexSelector selector, Context context, Genome input) {
        final int length = input.genes.length;
        if (length > 1) {
            final int index1 = selector.select(rng, length);
            final int index2 = selectSecondIndex(rng, selector, index1, length);
            final Genome result = new Genome(input);
            final Gene tmp = result.genes[index1];
            result.genes[index1] = result.genes[index2];
            result.genes[index2] = tmp;
            return result;
        }
        return input;
    }

}
