package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
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
    public Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input) {
        final Gene[] inputGenes = input.genes;
        final int length = inputGenes.length;
        if (length > 1) {
            final int index1 = config.getEvolutionContext().getGeneIndexSelector().select(rng, length);
            final int index2 = selectSecondIndex(rng, config.getEvolutionContext().getGeneIndexSelector(), index1, length);
            final Gene[] newGenes = new Gene[inputGenes.length];
            System.arraycopy(inputGenes, 0, newGenes, 0, inputGenes.length);
            final Gene tmp = newGenes[index1];
            newGenes[index1] = newGenes[index2];
            newGenes[index2] = tmp;
            return new Genome(newGenes, false);
        }
        return input;
    }

}
