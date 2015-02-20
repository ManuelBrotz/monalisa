package ch.brotzilla.monalisa.evolution.strategies;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class ProgressiveAddPolygonStrategy extends AbstractAddPolygonStrategy {

    @Override
    protected long computeTimeToNextPolygon(MersenneTwister rng, VectorizerConfig config, Genome input) {
        if (input.countPolygons() < 25) {
            return 180000;
        } else if (input.countPolygons() < 75) {
            return 360000;
        }
        return 600000;
    }

    public ProgressiveAddPolygonStrategy() {}

}
