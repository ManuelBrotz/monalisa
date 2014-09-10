package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class GenomeRemoveGeneMutation extends BasicMutation implements GenomeMutation {

    public GenomeRemoveGeneMutation() {
        super("remove-gene", "Remove Gene", "Removes a randomly chosen gene from the passed genome");
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        final Gene[] inputGenes = input.genes;
        final int length = inputGenes.length;
        if (length > 1) {
            final int index = evolutionContext.getGeneIndexSelector().select(rng, length);
            final Gene[] newGenes = new Gene[length - 1];
            if (index == 0) {
                System.arraycopy(inputGenes, 1, newGenes, 0, length - 1);
            } else if (index == length - 1) {
                System.arraycopy(inputGenes, 0, newGenes, 0, length - 1);
            } else {
                System.arraycopy(inputGenes, 0, newGenes, 0, index);
                System.arraycopy(inputGenes, index + 1, newGenes, index, length - index - 1);
            }
            return new Genome(newGenes, false);
        }
        return input;
    }

}
