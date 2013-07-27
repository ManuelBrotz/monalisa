package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneAlphaChannelMutation extends BasicMutation implements GeneMutation {

    public GeneAlphaChannelMutation(double probability) {
        super(probability);
    }

    @Override
    public Gene apply(MersenneTwister rng, Constraints constraints, Gene input) {
        final Gene result = new Gene(input);
        int value = result.color[0] + (rng.nextInt(51) - 25);
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return result;
    }
    
}