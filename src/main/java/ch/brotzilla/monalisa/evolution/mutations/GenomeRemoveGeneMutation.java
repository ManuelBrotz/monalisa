package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class GenomeRemoveGeneMutation extends BasicMutation implements GenomeMutation {

    public GenomeRemoveGeneMutation() {
        super("remove-gene", "Remove Gene", "Removes a randomly chosen gene from the passed genome");
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        final Gene[] inputLayer = input.getCurrentLayer();
        final int length = inputLayer.length;
        if (length > 1) {
            final int index = evolutionContext.getGeneIndexSelector().select(rng, length);
            final Gene[] newLayer = new Gene[length - 1];
            if (index == 0) {
                System.arraycopy(inputLayer, 1, newLayer, 0, length - 1);
            } else if (index == length - 1) {
                System.arraycopy(inputLayer, 0, newLayer, 0, length - 1);
            } else {
                System.arraycopy(inputLayer, 0, newLayer, 0, index);
                System.arraycopy(inputLayer, index + 1, newLayer, index, length - index - 1);
            }
            return new Genome(input.background, Utils.copyGenesReplaceLastLayer(input.genes, newLayer), false);
        }
        return input;
    }

}
