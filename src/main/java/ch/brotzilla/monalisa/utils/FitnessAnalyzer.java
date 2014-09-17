package ch.brotzilla.monalisa.utils;

import java.util.ArrayList;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class FitnessAnalyzer {

    protected final VectorizerConfig config;
    protected final Renderer renderer;
    protected final int[] targetData, importanceMap;
    
    protected double computeFitness(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        renderer.render(genome);
        if (renderer.getAutoUpdateBuffer()) {
            return config.getFitnessFunction().compute(config, genome, renderer.getBuffer());
        } else {
            return config.getFitnessFunction().compute(config, genome, renderer.readData());
        }
    }
    
    public FitnessAnalyzer(VectorizerConfig config) {
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        config.checkReady();
        this.config = config;
        this.renderer = config.createRenderer();
        this.targetData = config.getVectorizerContext().getTargetImageData();
        this.importanceMap = config.getVectorizerContext().getImportanceMapData();
    }
    
    public double[] analyze(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        final double originalFitness = computeFitness(genome);
        final int len = genome.genes.length;
        final double[] result = new double[len];
        for (int i = 0; i < len; i++) {
            final Gene[] newGenes = new Gene[len - 1];
            for (int j = 0, k = 0; j < len; j++) {
                if (j != i) {
                    newGenes[k] = genome.genes[j];
                    ++k;
                }
            }
            result[i] = originalFitness - computeFitness(new Genome(newGenes));
        }
        return result;
    }
    
    public Genome reduce(Genome genome) {
        final double[] result = analyze(genome);
        final ArrayList<Gene> list = new ArrayList<Gene>(result.length);
        for (int i = 0; i < result.length; i++) {
            if (result[i] < 0) {
                list.add(genome.genes[i]);
            }
        }
        final Genome reduced = new Genome(list.toArray(new Gene[list.size()]));
        reduced.fitness = computeFitness(reduced);
        return reduced;
    }
}
