package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class GenePointMutation extends BasicMutation implements GeneMutation {

    public GenePointMutation() {
        super("move-point", "Move Point", "Mutates a randomly chosen point of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerConfig config, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene result = new Gene(input);
        final int coord = rng.nextInt(result.x.length);
        result.x[coord] += config.getEvolutionContext().getPointMutationRange().select(rng);
        result.y[coord] += config.getEvolutionContext().getPointMutationRange().select(rng);
        return result;
    }
    
}