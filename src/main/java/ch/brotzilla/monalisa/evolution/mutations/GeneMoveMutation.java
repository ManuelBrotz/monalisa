package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.BoundingBox;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class GeneMoveMutation extends BasicMutation implements GeneMutation {

    public GeneMoveMutation() {
        super("move-gene", "Move Gene", "Moves the passed gene to a random point");
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerConfig config, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final BoundingBox box = input.computeBoundingBox();
        if (config.getWidth() <= box.getWidth() || config.getHeight() <= box.getHeight()) {
            return input;
        }
        final Gene result = new Gene(input);
        final int dx = rng.nextInt(config.getWidth() - box.getWidth()) - box.getXMin();
        final int dy = rng.nextInt(config.getHeight() - box.getHeight()) - box.getYMin();
        for (int i = 0; i < result.x.length; i++) {
            result.x[i] += dx;
            result.y[i] += dy;
        }
        return result;
    }
    
}