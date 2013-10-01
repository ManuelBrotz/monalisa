package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneAlphaChannelMutation extends BasicMutation implements GeneMutation {

    public GeneAlphaChannelMutation() {
        super("alpha-channel", "Alpha Channel", "Mutates the alpha channel of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Context context, Gene input) {
        final Gene result = new Gene(input);
        int value = result.color[0] + (rng.nextInt(51) - 25);
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return result;
    }
    
}