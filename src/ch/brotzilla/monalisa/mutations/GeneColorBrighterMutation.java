package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneColorBrighterMutation extends BasicMutation implements GeneMutation {

    public GeneColorBrighterMutation() {
        super("brighter-color", "Brighter Color", "Brightens the color of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Constraints constraints, Gene input) {
        final Gene result = new Gene(input);
        final float factor = 1.01f + (0.49f * rng.nextFloat());
        float r = (result.color[1] + 1) * factor;
        float g = (result.color[2] + 1) * factor;
        float b = (result.color[3] + 1) * factor;
        if (r > 255) result.color[1] = 255; else result.color[1] = Math.round(r);
        if (g > 255) result.color[2] = 255; else result.color[2] = Math.round(g);
        if (b > 255) result.color[3] = 255; else result.color[3] = Math.round(b);
        return result;
    }
    
}