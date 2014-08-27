package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

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
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        final Gene[] inputLayer = input.getCurrentLayer();
        final int length = inputLayer.length;
        if (length > 1) {
            final int index1 = evolutionContext.getGeneIndexSelector().select(rng, length);
            final int index2 = selectSecondIndex(rng, evolutionContext.getGeneIndexSelector(), index1, length);
            final Gene[] newLayer = new Gene[inputLayer.length];
            System.arraycopy(inputLayer, 0, newLayer, 0, inputLayer.length);
            final Gene tmp = newLayer[index1];
            newLayer[index1] = newLayer[index2];
            newLayer[index2] = tmp;
            return new Genome(input.background, Utils.copyGenesReplaceLastLayer(input.genes, newLayer), false);
        }
        return input;
    }

}
