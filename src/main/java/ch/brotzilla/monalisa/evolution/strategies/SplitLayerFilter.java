package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFilter;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class SplitLayerFilter implements GenomeFilter {

    protected final int genesPerLayer;
    protected final long timePerLayer;
    
    protected int startNumberOfGenes = -1;
    protected long startTime;
    
    protected void printMsg(Genome genome) {
        final StringBuilder b = new StringBuilder();
        for (final Gene[] layer : genome.genes) {
            b.append("[" + layer.length + "]");
        }
        System.out.println("Genome filtered: Layers = " + genome.genes.length + ", " + b + ", Polygons = " + genome.countPolygons());
    }
    
    public SplitLayerFilter(int genesPerLayer, long timePerLayer) {
        Preconditions.checkArgument(genesPerLayer > 0, "The parameter 'genesPerLayer' has to be greater than zero");
        Preconditions.checkArgument(timePerLayer > 0, "The parameter 'timePerLayer' has to be greater than zero");
        this.genesPerLayer = genesPerLayer;
        this.timePerLayer = timePerLayer;
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        if (startNumberOfGenes == -1) {
            startNumberOfGenes = input.countPolygons();
            startTime = System.currentTimeMillis();
            return input;
        }
        
        if (input.countPolygons() >= startNumberOfGenes + (genesPerLayer * 2) && System.currentTimeMillis() - startTime >= timePerLayer) {
            startNumberOfGenes = input.countPolygons();
            startTime = System.currentTimeMillis();
            final Genome result = Utils.splitCurrentLayerIntoNewLayer(input, genesPerLayer);
            result.fitness = input.fitness;
            return result;
        }
        
        return input;
    }
}
