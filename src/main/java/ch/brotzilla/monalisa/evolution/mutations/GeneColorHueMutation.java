package ch.brotzilla.monalisa.evolution.mutations;

import java.awt.Color;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class GeneColorHueMutation extends BasicMutation implements GeneMutation {

    public GeneColorHueMutation() {
        super("color-hue-change", "Color Hue Change", "Changes the hue the color of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerConfig config, Gene input) {
        final float[] hsb = Color.RGBtoHSB(input.color[1], input.color[2], input.color[3], null);
        final Color rgb = Color.getHSBColor(rng.nextFloat(), hsb[1], hsb[2]);
        final Gene result = new Gene(input, new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), input.color[0]));
        return result;
    }
    
}