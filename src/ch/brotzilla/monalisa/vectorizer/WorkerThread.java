package ch.brotzilla.monalisa.vectorizer;

import java.util.concurrent.ExecutorService;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.CachingRenderer;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.utils.Utils;

public class WorkerThread extends BasicThread {

    @Override
    protected void execute() {
        
        final Vectorizer v = getOwner();
        final SessionManager sm = v.getSessionManager();
        final VectorizerContext vc = sm.getVectorizerContext();
        final EvolutionContext ec = v.getEvolutionContext();
        
        final MersenneTwister rng = new MersenneTwister(sm.getParams().getSeed());
        final CachingRenderer renderer = new CachingRenderer(getOwner().getPolygonCache(), vc.getWidth(), vc.getHeight(), true);

        Genome genome = v.submit(null);
        while (!getExecutor().isShutdown()) {
            try {
                genome = v.submit(genome);
                if (genome == null) {
                    genome = new Genome(backgroundColor, Utils.createRandomGenes(rng, vc, ec, 10, 20));
                } else {
                    genome = strategy.apply(rng, vc, ec, genome);
                }
                renderer.render(genome);
                genome.fitness = Utils.computeSimpleFitness(genome, targetImage, importanceMap, renderer.getBuffer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public WorkerThread(Vectorizer owner, ExecutorService executor) {
        super(owner, executor);
    }

}
