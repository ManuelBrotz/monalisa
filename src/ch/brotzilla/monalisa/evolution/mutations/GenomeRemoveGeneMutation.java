package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;

public class GenomeRemoveGeneMutation extends BasicMutation implements GenomeMutation {

    public GenomeRemoveGeneMutation() {
        super("remove-gene", "Remove Gene", "Removes a randomly chosen gene from the passed genome");
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext context, EvolutionContext evolutionContext, Genome input) {
        final int length = input.genes.length;
        if (length > 1) {
            final int index = evolutionContext.getGeneIndexSelector().select(rng, length);
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
