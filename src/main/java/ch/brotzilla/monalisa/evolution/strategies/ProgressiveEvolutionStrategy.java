package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.AddPolygonStrategy;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.rendering.GenomeRenderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class ProgressiveEvolutionStrategy implements EvolutionStrategy {

    protected final AddPolygonStrategy addPolygonStrategy;
    
    protected GenomeRenderer renderer;
    protected int minPolygonsToAccept;
    
    public ProgressiveEvolutionStrategy(AddPolygonStrategy addPolygonStrategy) {
        Preconditions.checkNotNull(addPolygonStrategy, "The parameter 'addPolygonStrategy' must not be null");
        this.addPolygonStrategy = addPolygonStrategy;
    }
    
    public AddPolygonStrategy getAddPolygonStrategy() {
        return addPolygonStrategy;
    }
    
    @Override
    public Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input, boolean isImprovement) {
        if (renderer == null) {
            renderer = config.createRenderer();
            Preconditions.checkState(renderer.getAutoUpdateBuffer(), "The property 'getAutoUpdateBuffer()' of the renderer has to be true");
        }

        if (!isImprovement) {
            return input;
        }
        
        if (minPolygonsToAccept > 0 && input.countPolygons() < minPolygonsToAccept) {
            return null;
        }
        
        Genome result = Preconditions.checkNotNull(addPolygonStrategy.apply(rng, config, input), "The method getAddPolygonStrategy().apply() must not return null");
        
        if (result != input) {
            minPolygonsToAccept = result.countPolygons();
            result.overrideFitness = true;
            renderer.render(result);
            result.fitness = config.getFitnessFunction().compute(config, result, renderer.getBuffer());
        }
        
        return result;
    }

}
