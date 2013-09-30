package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneColorChannelMutation extends BasicMutation implements GeneMutation {

    public GeneColorChannelMutation() {
        super("color-channel", "Color Channel", "Mutates a randomly chosen color component of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Constraints constraints, Gene input) {
        final Gene result = new Gene(input);
        final int channel = rng.nextInt(3) + 1;
        int value = result.color[channel] + (rng.nextInt(51) - 25);
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return result;
    }
    
}