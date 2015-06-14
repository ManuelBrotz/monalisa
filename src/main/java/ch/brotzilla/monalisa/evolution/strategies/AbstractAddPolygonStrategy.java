package ch.brotzilla.monalisa.evolution.strategies;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.AddPolygonStrategy;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public abstract class AbstractAddPolygonStrategy implements AddPolygonStrategy {

    protected long timeLastPolygonAdded;
    protected long timeToNextPolygon;
    
    protected abstract long computeTimeToNextPolygon(MersenneTwister rng, VectorizerConfig config, Genome input);

    public AbstractAddPolygonStrategy() {}

    public long getTimeLastPolygonAdded() {
        return timeLastPolygonAdded;
    }

    public long getTimeToNextPolygon() {
        return timeToNextPolygon;
    }
    
    public long getTimeToNextPolygonLeft() {
        return System.currentTimeMillis() - timeLastPolygonAdded;
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input) {
        if (timeLastPolygonAdded == 0) {
            timeLastPolygonAdded = System.currentTimeMillis();
            timeToNextPolygon = computeTimeToNextPolygon(rng, config, input);
            System.out.println("New Polygon will be added in " + timeToNextPolygon + " ms.");
        }
        if (getTimeToNextPolygonLeft() >= timeToNextPolygon) {
            timeLastPolygonAdded = System.currentTimeMillis();
            timeToNextPolygon = computeTimeToNextPolygon(rng, config, input);
            System.out.println("New polygon added.");
            System.out.println("New Polygon will be added in " + timeToNextPolygon + " ms.");
            return Utils.appendGene(input, rng, config, null);
        }
        return input;
    }

}
