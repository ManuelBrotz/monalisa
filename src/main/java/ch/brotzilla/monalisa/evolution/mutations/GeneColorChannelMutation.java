package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class GeneColorChannelMutation extends BasicMutation implements GeneMutation {

    public GeneColorChannelMutation() {
        super("color-channel", "Color Channel", "Mutates a randomly chosen color component of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerConfig config, Gene input) {
        final Gene result = new Gene(input);
        final int channel = rng.nextInt(3) + 1;
        int value = result.color[channel] + config.getMutationConfig().getColorChannelMutationRange().select(rng);
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return result;
    }
    
}