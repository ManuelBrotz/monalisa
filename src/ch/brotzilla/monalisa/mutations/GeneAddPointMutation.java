package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneAddPointMutation extends BasicMutation implements GeneMutation {

    public GeneAddPointMutation(double probability) {
        super(probability);
    }

    @Override
    public Gene apply(MersenneTwister rng, Constraints constraints, Gene input) {
        final Gene result = new Gene(input);
        final int coord = rng.nextInt(result.x.length);
        final int dx = rng.nextInt(21) - 10, dy = rng.nextInt(21) - 10;
        result.x[coord] += dx;
        result.y[coord] += dy;
        return result;
    }
    
}