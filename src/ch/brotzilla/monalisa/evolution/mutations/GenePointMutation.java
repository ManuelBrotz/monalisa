package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GenePointMutation extends BasicMutation implements GeneMutation {

    public GenePointMutation() {
        super("move-point", "Move Point", "Mutates a randomly chosen point of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Context context, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene result = new Gene(input);
        final int coord = rng.nextInt(result.x.length);
        final int dx = rng.nextInt(21) - 10, dy = rng.nextInt(21) - 10;
        result.x[coord] += dx;
        result.y[coord] += dy;
        return result;
    }
    
}