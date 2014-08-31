package ch.brotzilla.monalisa.vectorizer;

import java.util.concurrent.ExecutorService;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.rendering.LayeredRenderer;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.util.MersenneTwister;

public class WorkerThread extends BasicThread {
    
    @Override
    protected void execute() {
        
        final Vectorizer v = getOwner();
        
        if (!v.isReady()) {
            throw new IllegalStateException("Vectorizer is not ready");
        }
        
        final VectorizerContext vc = v.getVectorizerContext();
        final EvolutionContext ec = v.getEvolutionContext();
        final EvolutionStrategy es = v.getEvolutionStrategy();
        final GenomeFactory gf = es.getGenomeFactory();
        
        if (gf == null) {
            throw new IllegalStateException("GenomeFactory must not be null");
        }
        
        final int[] targetImageData = vc.getTargetImageData();
        final int[] importanceMapData = vc.getImportanceMapData();
        
        final MersenneTwister rng = new MersenneTwister(v.nextSeed());
        final Renderer renderer = new LayeredRenderer(vc.getWidth(), vc.getHeight(), true);

        Genome genome = vc.getLatestGenome();
        if (genome == null) {
            genome = gf.createGenome(rng, vc, ec);
            if (genome == null) {
                throw new IllegalStateException("GenomeFactory must not return null");
            }
        }
        while (genome != null && !getExecutor().isShutdown()) {
            try {
                final Genome mutated = es.apply(rng, vc, ec, genome);
                if (mutated == null) {
                    throw new IllegalStateException("MutationStrategy must not return null");
                } else if (mutated == genome) {
                    continue;
                }
                renderer.render(mutated);
                mutated.fitness = Utils.computeSimpleFitness(mutated, targetImageData, importanceMapData, renderer.getBuffer());
                genome = v.submit(mutated);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public WorkerThread(Vectorizer owner, ExecutorService executor) {
        super(owner, executor);
    }

}
