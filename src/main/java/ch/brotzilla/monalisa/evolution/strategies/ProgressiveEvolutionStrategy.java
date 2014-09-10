package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.RendererFactory;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class ProgressiveEvolutionStrategy implements EvolutionStrategy {

    protected final RendererFactory rendererFactory;
    protected final GenomeFactory genomeFactory;
    protected Renderer renderer;
    
    protected int timeBetweenNewPolygons = 60000;
    protected long timeLastPolygonAdded = 0;
    protected int minPolygonsToAccept = 0;
    
    public ProgressiveEvolutionStrategy(RendererFactory rendererFactory, GenomeFactory genomeFactory) {
        Preconditions.checkNotNull(rendererFactory, "The parameter 'rendererFactory' must not be null");
        Preconditions.checkNotNull(genomeFactory, "The parameter 'genomeFactory' must not be null");
        this.rendererFactory = rendererFactory;
        this.genomeFactory = genomeFactory;
    }
    
    public int getTimeBetweenNewPolygons() {
        return timeBetweenNewPolygons;
    }
    
    public void setTimeBetweenNewPolygons(int value) {
        Preconditions.checkArgument(value > 0, "The parameter 'value' has to be greater than zero");
        this.timeBetweenNewPolygons = value;
    }
    
    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        Genome result = input;
        
        if (minPolygonsToAccept > 0 && input.countPolygons() < minPolygonsToAccept) {
            return null;
        }
        
        if (timeLastPolygonAdded == 0) {
            timeLastPolygonAdded = System.currentTimeMillis();
            System.out.println("New Polygon will be added in " + timeBetweenNewPolygons + " ms.");
        } else if (System.currentTimeMillis() - timeLastPolygonAdded >= timeBetweenNewPolygons) {
            timeLastPolygonAdded = 0;
            result = Utils.appendGene(result, rng, vectorizerContext, evolutionContext, genomeFactory);
            minPolygonsToAccept = result.countPolygons();
            System.out.println("New polygon added.");
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
