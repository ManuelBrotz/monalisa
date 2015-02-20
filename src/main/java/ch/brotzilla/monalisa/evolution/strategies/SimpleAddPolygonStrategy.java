package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class SimpleAddPolygonStrategy extends AbstractAddPolygonStrategy {

    protected int timeBetweenNewPolygons;

    @Override
    protected long computeTimeToNextPolygon(MersenneTwister rng, VectorizerConfig config, Genome input) {
        return timeBetweenNewPolygons;
    }

    public SimpleAddPolygonStrategy() {
        this(60000);
    }
    
    public SimpleAddPolygonStrategy(int timeBetweenNewPolygons) {
        Preconditions.checkArgument(timeBetweenNewPolygons > 0, "The parameter 'timeBetweenNewPolygons' has to be greater than zero");
        this.timeBetweenNewPolygons = timeBetweenNewPolygons;
    }

    public int getTimeBetweenNewPolygons() {
        return timeBetweenNewPolygons;
    }
    
    public void setTimeBetweenNewPolygons(int value) {
        Preconditions.checkArgument(value > 0, "The parameter 'value' has to be greater than zero");
        this.timeBetweenNewPolygons = value;
    }
    
}
