package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class ProgressiveEvolutionStrategy implements EvolutionStrategy {

    protected Renderer renderer;
    
    protected int timeBetweenNewPolygons = 60000;
    protected long timeLastPolygonAdded = 0;
    protected int minPolygonsToAccept = 0;
    
    public ProgressiveEvolutionStrategy() {
    }
    
    public int getTimeBetweenNewPolygons() {
        return timeBetweenNewPolygons;
    }
    
    public void setTimeBetweenNewPolygons(int value) {
        Preconditions.checkArgument(value > 0, "The parameter 'value' has to be greater than zero");
        this.timeBetweenNewPolygons = value;
    }
    
    @Override
    public Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input) {
        Genome result = input;
        
        if (minPolygonsToAccept > 0 && input.countPolygons() < minPolygonsToAccept) {
            return null;
        }
        
        if (timeLastPolygonAdded == 0) {
            timeLastPolygonAdded = System.currentTimeMillis();
            System.out.println("New Polygon will be added in " + timeBetweenNewPolygons + " ms.");
        } else if (System.currentTimeMillis() - timeLastPolygonAdded >= timeBetweenNewPolygons) {
            timeLastPolygonAdded = 0;
            result = Utils.appendGene(result, rng, config);
            minPolygonsToAccept = result.countPolygons();
            System.out.println("New polygon added.");
        }
        
        if (result != input) {
            result.overrideFitnessFlag = true;
            if (renderer == null) {
                renderer = config.createRenderer();
            }
            renderer.render(result);
            final VectorizerContext vc = config.getVectorizerContext();
            result.fitness = Utils.computeSimpleFitness(result, vc.getTargetImageData(), vc.getImportanceMapData(), renderer.getBuffer());
        }
        
        return result;
    }

}
