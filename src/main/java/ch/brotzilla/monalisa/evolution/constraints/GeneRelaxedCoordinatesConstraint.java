package ch.brotzilla.monalisa.evolution.constraints;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.evolution.strategies.MutationConfig;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class GeneRelaxedCoordinatesConstraint implements GeneConstraint {

    private final int minNumberOfPointsInside, maxNumberOfPointsOutside;
    
    public GeneRelaxedCoordinatesConstraint(int minNumberOfPointsInside, int maxNumberOfPointsOutside) {
        Preconditions.checkArgument(minNumberOfPointsInside >= 0, "The parameter 'minNumberOfPointsInside' has to be greater than or equal to zero");
        Preconditions.checkArgument(maxNumberOfPointsOutside >= 0, "The parameter 'maxNumberOfPointsOutside' has to be greater than or equal to zero");
        this.minNumberOfPointsInside = minNumberOfPointsInside;
        this.maxNumberOfPointsOutside = maxNumberOfPointsOutside;
    }
    
    public GeneRelaxedCoordinatesConstraint() {
        this(2, 2);
    }
    
    public int getMinNumberOfPointsInside() {
        return minNumberOfPointsInside;
    }
    
    public int getMaxNumberOfPointsOutside() {
        return maxNumberOfPointsOutside;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        final MutationConfig mc = config.getMutationConfig();
        final int len = gene.x.length;
        final int[] x = gene.x, y = gene.y;
        final int w = config.getWidth(), h = config.getHeight();
        final int ibx = mc.getInnerBorderX(), iby = mc.getInnerBorderY();
        final int ixmin = ibx, ixmax = w - ibx, iymin = iby, iymax = h - iby;
        final int minPointsInside = Math.min(minNumberOfPointsInside, len);
        int countOut = 0, countIn = 0;
        for (int i = 0; i < len; i++) {
            final int px = x[i], py = y[i];
            if (px < 0 || px > w || py < 0 || py > h) {
                ++countOut;
                if (countOut > maxNumberOfPointsOutside) {
                    return false;
                }
            }
            if (px >= ixmin && px < ixmax && py >= iymin && py < iymax) {
                ++countIn;
            }
        }
        return (countOut <= maxNumberOfPointsOutside) && (countIn >= minPointsInside);
    }

}
