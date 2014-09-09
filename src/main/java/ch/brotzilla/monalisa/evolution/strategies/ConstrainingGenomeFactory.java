package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class ConstrainingGenomeFactory implements GenomeFactory {

    private final int minGenes, maxGenes;

    public ConstrainingGenomeFactory(int minGenes, int maxGenes) {
        Preconditions.checkArgument(minGenes >= 0, "The parameter 'minGenes' has to be greater than or equal to zero");
        Preconditions.checkArgument(maxGenes > 0, "The parameter 'maxGenes' has to be greater than zero");
        Preconditions.checkArgument(maxGenes >= minGenes, "The parameter 'maxGenes' has to be greater than or equal to the parameter 'minGenes'");
        this.minGenes = minGenes;
        this.maxGenes = maxGenes;
    }
    
    public int getMinGenes() {
        return minGenes;
    }
    
    public int getMaxGenes() {
        return maxGenes;
    }
    
    @Override
    public Gene createGene(MersenneTwister rng, VectorizerContext vc, EvolutionContext ec) {
        Gene result = Utils.createRandomGene(rng, vc, ec);
        while (!(Utils.hasAcceptableAlpha(result, 10, 245) 
                && Utils.hasAcceptableCoordinates(result, vc, ec) 
                && Utils.hasAcceptableAngles(result, 15.0d) 
                && Utils.hasAcceptablePointToLineDistances(result, 5.0d))) {
            result = Utils.createRandomGene(rng, vc, ec);
        }
        return result;
    }

    @Override
    public Genome createGenome(MersenneTwister rng, VectorizerContext vc, EvolutionContext ec) {
        return new Genome(null, new Gene[][] {Utils.createRandomGenes(rng, vc, ec, minGenes, maxGenes, this)});
    }

}
