package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.constraints.MutationConstraints;
import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.genes.RectConstraint;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class BasicGenomeFactory implements GenomeFactory {

    private final int minGenes, maxGenes;

    public BasicGenomeFactory(int minGenes, int maxGenes) {
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
    public Gene createGene(MersenneTwister rng, VectorizerConfig config, RectConstraint constraint) {
        final MutationConstraints c = config.getConstraints();
        Gene result = Utils.createRandomGene(rng, config, constraint);
        while (!c.satisfied(config, result)) {
            result = Utils.createRandomGene(rng, config, constraint);
        }
        return result;
    }

    @Override
    public Genome createGenome(MersenneTwister rng, VectorizerConfig config) {
        final MutationConstraints c = config.getConstraints();
        Genome result = new Genome(Utils.createRandomGenes(rng, config, minGenes, maxGenes, this, null));
        while (!c.satisfied(config, result)) {
            result = new Genome(Utils.createRandomGenes(rng, config, minGenes, maxGenes, this, null));
        }
        return result;
    }

}
