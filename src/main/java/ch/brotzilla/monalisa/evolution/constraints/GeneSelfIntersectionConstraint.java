package ch.brotzilla.monalisa.evolution.constraints;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneConstraint;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class GeneSelfIntersectionConstraint implements GeneConstraint {

    public GeneSelfIntersectionConstraint() {
    }

    @Override
    public boolean satisfied(VectorizerConfig config, Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        return !Utils.isSelfIntersecting(gene);
    }

}
