package ch.brotzilla.monalisa.evolution.constraints;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.Geometry;

public class GeneAngleConstraint implements GeneConstraint {

    private final double minAngleInDegrees;
    
    public GeneAngleConstraint(double minAngleInDegrees) {
        Preconditions.checkArgument(minAngleInDegrees >= 0, "The parameter 'minAngleInDegrees' has to be greater than or equal to zero");
        this.minAngleInDegrees = minAngleInDegrees;
    }
    
    public double getMinAngleInDegrees() {
        return minAngleInDegrees;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        final int[] x = gene.x, y = gene.y;
        final int len = x.length;
        final int last = len - 1;
        final double[] p0 = new double[] {0, 0, 0}, p1 = new double[] {0, 0, 0}, p2 = new double[] {0, 0, 0};
        for (int i = 0; i < len; i++) {
            p0[0] = x[i];
            p0[1] = y[i];
            p1[0] = (i == 0) ? x[last] : x[i - 1];
            p1[1] = (i == 0) ? y[last] : y[i - 1];
            p2[0] = (i == last) ? x[0] : x[i + 1];
            p2[1] = (i == last) ? y[0] : y[i + 1];
            double angle = Math.toDegrees(Geometry.computeAngle(p0, p1, p2));
            if (angle < minAngleInDegrees) {
                return false;
            }
        }
        return true;
    }

}
