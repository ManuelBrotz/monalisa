package ch.brotzilla.monalisa.evolution.filters;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFilter;
import ch.brotzilla.monalisa.evolution.intf.RendererFactory;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class LayeredStrategyFilter implements GenomeFilter {

    protected final RendererFactory rendererFactory;
    protected Renderer renderer;
    
    protected int initialLayerSize = 1;
    protected int maxLayerSize = 15;
    protected int timeBetweenNewPolygons = 60000;
    protected int timeForFinalOptimization = 5 * 60000; 
    
    protected long timeLastPolygonAdded = -1;
    protected long timeStartFinalOptimization = -1;
    
    protected int minPolygonsToAccept = -1;
    
    public LayeredStrategyFilter(RendererFactory rendererFactory) {
        Preconditions.checkNotNull(rendererFactory, "The parameter 'rendererFactory' must not be null");
        this.rendererFactory = rendererFactory;
    }
    
    public int getInitialLayerSize() {
        return initialLayerSize;
    }
    
    public void setInitialLayerSize(int value) {
        Preconditions.checkArgument(value > 0, "The parameter 'value' has to be greater than zero");
        Preconditions.checkArgument(value < getMaxLayerSize(), "The parameter 'value' has to be less than getMaxLayerSize()");
        this.initialLayerSize = value;
    }
    
    public int getMaxLayerSize() {
        return maxLayerSize;
    }
    
    public void setMaxLayerSize(int value) {
        Preconditions.checkArgument(value > getInitialLayerSize(), "The parameter 'value' has to be greater than getInitialLayerSize()");
        this.maxLayerSize = value;
    }
    
    public void setLayerSizes(int initialLayerSize, int maxLayerSize) {
        Preconditions.checkArgument(initialLayerSize > 0, "The parameter 'initialLayerSize' has to be greater than zero");
        Preconditions.checkArgument(initialLayerSize < maxLayerSize, "The parameter 'initialLayerSize' has to be less than the parameter 'maxLayerSize'");
        this.initialLayerSize = initialLayerSize;
        this.maxLayerSize = maxLayerSize;
    }
    
    public int getTimeBetweenNewPolygons() {
        return timeBetweenNewPolygons;
    }
    
    public void setTimeBetweenNewPolygons(int value) {
        Preconditions.checkArgument(value > 0, "The parameter 'value' has to be greater than zero");
        this.timeBetweenNewPolygons = value;
    }
    
    public int getTimeForFinalOptimization() {
        return timeForFinalOptimization;
    }
    
    public void setTimeForFinalOptimization(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.timeForFinalOptimization = value;
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        Genome result = input;
        
        if (minPolygonsToAccept > 0 && input.countPolygons() < minPolygonsToAccept) {
            return null;
        }
        
        if (input.getCurrentLayer().length < maxLayerSize) {
            if (timeLastPolygonAdded < 0) {
                timeLastPolygonAdded = System.currentTimeMillis();
                System.out.println("New Polygon will be added in " + timeBetweenNewPolygons + " ms.");
            } else if (System.currentTimeMillis() - timeLastPolygonAdded >= timeBetweenNewPolygons) {
                timeLastPolygonAdded = -1;
                result = Utils.appendGeneToCurrentLayer(result, rng, vectorizerContext, evolutionContext);
                minPolygonsToAccept = result.countPolygons();
//                Preconditions.checkState(result != null, "Internal error");
//                Preconditions.checkState(result != input, "Internal error");
//                Preconditions.checkState(result.genes.length == input.genes.length, "Internal error");
//                Preconditions.checkState(result.getCurrentLayer().length == input.getCurrentLayer().length + 1, "Internal error");
                System.out.println("New polygon added.");
            }
        } else {
            if (timeStartFinalOptimization < 0) {
                timeStartFinalOptimization = System.currentTimeMillis();
                System.out.println("Final optimization will last for " + timeForFinalOptimization + " ms.");
            } else if (System.currentTimeMillis() - timeStartFinalOptimization >= timeForFinalOptimization) {
                timeStartFinalOptimization = -1;
                final Gene[] newLayer = Utils.createRandomGenes(rng, vectorizerContext, evolutionContext, initialLayerSize, initialLayerSize);
                result = new Genome(result.background, Utils.copyGenesAppendLayer(result.genes, newLayer));
                minPolygonsToAccept = result.countPolygons();
                System.out.println("Final optimization done. New layer added.");
            }
        }
        
        if (result != input) {
            result.overrideFitnessFlag = true;
            if (renderer == null) {
                renderer = rendererFactory.createRenderer(vectorizerContext, evolutionContext);
            }
            renderer.render(result);
            result.fitness = Utils.computeSimpleFitness(result, vectorizerContext.getTargetImageData(), vectorizerContext.getImportanceMapData(), renderer.getBuffer());
        }
        return result;
    }

}
