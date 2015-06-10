package ch.brotzilla.monalisa.evolution.fitness;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.FitnessFunction;
import ch.brotzilla.monalisa.rendering.GenomeRenderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public abstract class AbstractFitnessFunction implements FitnessFunction {

    @Override
    public double compute(VectorizerConfig config, Genome genome) {
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        final GenomeRenderer renderer = config.createRenderer();
        renderer.render(genome);
        if (renderer.getAutoUpdateBuffer()) {
            return compute(config, genome, renderer.getBuffer());
        }
        return compute(config, genome, renderer.readData());
    }

}
