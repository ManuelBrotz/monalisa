package ch.brotzilla.monalisa.evolution.strategies;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class BasicMutationStrategy implements MutationStrategy {

    public BasicMutationStrategy() {
    }

    @Override
    public Genome mutate(MersenneTwister rng, VectorizerConfig config, Genome input) {
        return null;
    }

}
