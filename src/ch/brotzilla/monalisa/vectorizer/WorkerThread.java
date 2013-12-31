package ch.brotzilla.monalisa.vectorizer;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.CachingRenderer;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.utils.Utils;

public class WorkerThread extends BasicThread {

    private Genome createRandomGenome(MersenneTwister rng, VectorizerContext vc, EvolutionContext ec) {
        return new Genome(null, Utils.createRandomGenes(rng, vc, ec, 10, 20));
    }
    
    private int[] getImportanceMapData(int[] data, int length) {
        if (data == null) {
            final int[] result = new int[length];
            Arrays.fill(result, 255);
            return result;
        }
        return data;
    }
    
    @Override
    protected void execute() {
        
        final Vectorizer v = getOwner();
        final SessionManager sm = v.getSessionManager();
        final VectorizerContext vc = sm.getVectorizerContext();
        final EvolutionContext ec = v.getEvolutionContext();
        final MutationStrategy strategy = v.getMutationStrategy();
        
        final int[] targetImageData = sm.getTargetImage().getBuffer();
        final int[] importanceMapData = getImportanceMapData(sm.getImportanceMap().getBuffer(), targetImageData.length);
        
        final MersenneTwister rng = new MersenneTwister(sm.getParams().getSeed());
        final CachingRenderer renderer = new CachingRenderer(getOwner().getPolygonCache(), vc.getWidth(), vc.getHeight(), true);

        Genome genome = createRandomGenome(rng, vc, ec);
        while (!getExecutor().isShutdown()) {
            try {
                genome = strategy.apply(rng, vc, ec, genome);
                renderer.render(genome);
                genome.fitness = Utils.computeSimpleFitness(genome, targetImageData, importanceMapData, renderer.getBuffer());
                genome = v.submit(genome);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public WorkerThread(Vectorizer owner, ExecutorService executor) {
        super(owner, executor);
    }

}
