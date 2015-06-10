package ch.brotzilla.monalisa.evolution.constraints;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class GeneStrictCoordinatesConstraint implements GeneConstraint {

    public GeneStrictCoordinatesConstraint() {
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        final int len = gene.x.length;
        final int[] x = gene.x, y = gene.y;
        final int w = config.getWidth(), h = config.getHeight();
        for (int i = 0; i < len; i++) {
            final int px = x[i], py = y[i];
            if (px < 0 || px > w || py < 0 || py > h) {
                return false;
            }
        }
        return true;
    }

}
