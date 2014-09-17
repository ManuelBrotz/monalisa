package ch.brotzilla.monalisa.evolution.constraints;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.Geometry;

public class GeneVertexToEdgeDistanceConstraint implements GeneConstraint {

    private final double minDistance;
    
    public GeneVertexToEdgeDistanceConstraint(double minDistance) {
        Preconditions.checkArgument(minDistance >= 0, "The parameter 'minDistance' has to be greater than or equal to zero");
        this.minDistance = minDistance;
    }
    
    public double getMinDistance() {
        return minDistance;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        final int len = gene.x.length;
        final int last = len - 1;
        final int lines = len - 2;
        final int[] x = gene.x, y = gene.y;
        for (int pointIndex = 0; pointIndex < len; pointIndex++) {
            int lineIndex = (pointIndex == last) ? 0 : pointIndex + 1;
            for (int line = 0; line < lines; line++) {
                final int px = x[pointIndex];
                final int py = y[pointIndex];
                final int x0 = x[lineIndex];
                final int y0 = y[lineIndex];
                final int x1 = x[lineIndex == last ? 0 : lineIndex + 1];
                final int y1 = y[lineIndex == last ? 0 : lineIndex + 1];
                if (Geometry.distance(x0, y0, x1, y1, px, py) < minDistance) {
                    return false;
                }
                lineIndex = (lineIndex == last) ? 0 : lineIndex + 1;
            }
        }
        return true;
    }

}
