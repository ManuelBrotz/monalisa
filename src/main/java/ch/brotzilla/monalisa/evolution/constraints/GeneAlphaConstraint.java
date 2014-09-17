package ch.brotzilla.monalisa.evolution.constraints;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class GeneAlphaConstraint implements GeneConstraint {

    private final int minAlpha, maxAlpha;
    
    public GeneAlphaConstraint(int minAlpha, int maxAlpha) {
        Preconditions.checkArgument(minAlpha >= 0, "The parameter 'minAlpha' has to be greater than or equal to zero");
        Preconditions.checkArgument(maxAlpha <= 255, "The parameter 'maxAlpha' has to be less than or equal to 255");
        Preconditions.checkArgument(minAlpha < maxAlpha, "The parameter 'minAlpha' has to be less than the parameter 'maxAlpha'");
        this.minAlpha = minAlpha;
        this.maxAlpha = maxAlpha;
    }
    
    public int getMinAlpha() {
        return minAlpha;
    }
    
    public int getMaxAlpha() {
        return maxAlpha;
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        final int alpha = gene.color[0];
        return (alpha >= minAlpha) && (alpha <= maxAlpha);
    }

}
