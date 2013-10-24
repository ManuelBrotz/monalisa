package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.vectorizer.Context;

public class GeneColorChannelMutation extends BasicMutation implements GeneMutation {

    public GeneColorChannelMutation() {
        super("color-channel", "Color Channel", "Mutates a randomly chosen color component of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Context context, EvolutionContext evolutionContext, Gene input) {
        final Gene result = new Gene(input);
        final int channel = rng.nextInt(3) + 1;
        int value = result.color[channel] + evolutionContext.getColorChannelMutationRange().select(rng);
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return result;
    }
    
}