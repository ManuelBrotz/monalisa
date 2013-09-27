package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.mutations.intf.GeneSelector;
import ch.brotzilla.monalisa.mutations.intf.GenomeMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GenomeRemoveGeneMutation extends BasicMutation implements GenomeMutation {

    public GenomeRemoveGeneMutation(double probability) {
        super(probability);
    }

    @Override
    public Genome apply(MersenneTwister rng, GeneSelector selector, Constraints constraints, Genome input) {
        final int length = input.genes.length;
        if (length > 1) {
            final int index = selector.select(rng, length);
            final Gene[] genes = new Gene[length - 1];
            if (index == 0) {
                System.arraycopy(input.genes, 1, genes, 0, length - 1);
            } else if (index == length - 1) {
                System.arraycopy(input.genes, 0, genes, 0, length - 1);
            } else {
                System.arraycopy(input.genes, 0, genes, 0, index);
                System.arraycopy(input.genes, index + 1, genes, index, length - index - 1);
            }
            return new Genome(input.background, genes);
        }
        return input;
    }

}
