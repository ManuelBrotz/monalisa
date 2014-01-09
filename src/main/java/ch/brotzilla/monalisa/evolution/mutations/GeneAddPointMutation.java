package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class GeneAddPointMutation extends BasicMutation implements GeneMutation {

    public GeneAddPointMutation() {
        super("add-point", "Add Point", "Adds a random point to the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Gene input) {
        final int len = input.x.length;
        final int index = rng.nextInt(len+1);
        final int prev = index == 0 ? len : index - 1;
        final int next = index == len ? 0 : index + 1;
        final int[] x = expand(input.x, index);
        final int[] y = expand(input.y, index);
        x[index] = Math.round((x[prev] + x[next]) / 2.0f) + evolutionContext.getPointMutationRange().select(rng);
        y[index] = Math.round((y[prev] + y[next]) / 2.0f) + evolutionContext.getPointMutationRange().select(rng);
        return new Gene(x, y, input.color);
    }
    
    private int[] expand(int[] input, int index) {
        final int len = input.length;
        final int[] result = new int[len + 1];
        if (index == 0) {
            System.arraycopy(input, 0, result, 1, len);
        } else if (index == len) {
            System.arraycopy(input, 0, result, 0, len);
        } else {
            System.arraycopy(input, 0, result, 0, index);
            System.arraycopy(input, index, result, index + 1, len - index);
        }
        return result;
    }
    
}