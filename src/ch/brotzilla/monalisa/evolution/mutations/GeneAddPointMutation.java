package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;

public class GeneAddPointMutation extends BasicMutation implements GeneMutation {

    public GeneAddPointMutation() {
        super("add-point", "Add Point", "Adds a random point to the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerContext context, EvolutionContext evolutionContext, Gene input) {
        final int len = input.x.length;
        final int[] x = new int[len + 1];
        final int[] y = new int[len + 1];
        System.arraycopy(input.x, 0, x, 0, len);
        System.arraycopy(input.y, 0, y, 0, len);
        x[len] = Math.round((x[0] + x[len-1]) / 2.0f) + evolutionContext.getPointMutationRange().select(rng);
        y[len] = Math.round((y[0] + y[len-1]) / 2.0f) + evolutionContext.getPointMutationRange().select(rng);
        return new Gene(x, y, input.color);
    }
    
}