package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneColorDarkerMutation extends BasicMutation implements GeneMutation {

    public GeneColorDarkerMutation() {
        super("darker-color", "Darker Color", "Darkens the color of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Context context, EvolutionContext evolutionContext, Gene input) {
        final Gene result = new Gene(input);
        final float factor = 0.5f + (0.49f * rng.nextFloat());
        float r = (result.color[1] + 1) * factor;
        float g = (result.color[2] + 1) * factor;
        float b = (result.color[3] + 1) * factor;
        if (r > 255) result.color[1] = 255; else result.color[1] = Math.round(r);
        if (g > 255) result.color[2] = 255; else result.color[2] = Math.round(g);
        if (b > 255) result.color[3] = 255; else result.color[3] = Math.round(b);
        return result;
    }
    
}