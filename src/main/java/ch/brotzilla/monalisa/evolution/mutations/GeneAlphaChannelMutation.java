package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class GeneAlphaChannelMutation extends BasicMutation implements GeneMutation {

    public GeneAlphaChannelMutation() {
        super("alpha-channel", "Alpha Channel", "Mutates the alpha channel of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerConfig config, Gene input) {
        final Gene result = new Gene(input);
        int value = result.color[0] + config.getMutationConfig().getColorChannelMutationRange().select(rng);
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return result;
    }
    
}