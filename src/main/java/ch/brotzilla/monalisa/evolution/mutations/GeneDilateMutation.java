package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.BoundingBox;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class GeneDilateMutation extends BasicMutation implements GeneMutation {

    public GeneDilateMutation() {
        super("dilate", "Dilate", "Dilates the passed gene over a random center point and by a random dilation factor");
    }
    
    public GeneDilateMutation(String id, String name, String description) {
        super(id, name, description);
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Gene input) {
        final BoundingBox box = Utils.computeBoundingBox(input, 2.0d, 2.0d);
        final int cx = box.getXMin() + rng.nextInt(box.getWidth());
        final int cy = box.getYMin() + rng.nextInt(box.getHeight());
        final double sf = 0.75d + rng.nextDouble() * 0.5d;
        final int len = input.x.length;
        final int[] gx = input.x, gy = input.y;
        final int[] nx = new int[len], ny = new int[len];
        for (int i = 0; i < len; i++) {
            final int vx = gx[i] - cx, vy = gy[i] - cy;
            nx[i] = cx + (int) Math.round(vx * sf);
            ny[i] = cy + (int) Math.round(vy * sf);
        }
        return new Gene(nx, ny, input.color);
    }

}
