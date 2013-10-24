package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;

public class GenePointMutation extends BasicMutation implements GeneMutation {

    public GenePointMutation() {
        super("move-point", "Move Point", "Mutates a randomly chosen point of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerContext context, EvolutionContext evolutionContext, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene result = new Gene(input);
        final int coord = rng.nextInt(result.x.length);
        result.x[coord] += evolutionContext.getPointMutationRange().select(rng);
        result.y[coord] += evolutionContext.getPointMutationRange().select(rng);
        return result;
    }
    
}