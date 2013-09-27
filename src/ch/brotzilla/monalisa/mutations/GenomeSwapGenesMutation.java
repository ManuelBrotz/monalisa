package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.mutations.intf.GeneSelector;
import ch.brotzilla.monalisa.mutations.intf.GenomeMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GenomeSwapGenesMutation extends BasicMutation implements GenomeMutation {

    private int selectSecondIndex(MersenneTwister rng, GeneSelector selector, int firstIndex, int length) {
        int result = firstIndex;
        while (result == firstIndex) {
            result = selector.select(rng, length);
        }
        return result;
    }
    
    public GenomeSwapGenesMutation(double probability) {
        super(probability);
    }

    @Override
    public Genome apply(MersenneTwister rng, GeneSelector selector, Constraints constraints, Genome input) {
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
