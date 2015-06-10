package ch.brotzilla.monalisa.vectorizer;

import java.util.concurrent.ExecutorService;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.FitnessFunction;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.rendering.GenomeRenderer;
import ch.brotzilla.util.MersenneTwister;

public class WorkerThread extends BasicThread {
    
    @Override
    protected void execute() {
        
        final Vectorizer v = getOwner();
        final VectorizerConfig c = v.getConfig();
        
        final VectorizerContext vc = c.getVectorizerContext();
        final MutationStrategy ms = c.getMutationStrategy();
        final GenomeFactory gf = c.getGenomeFactory();
        final GenomeRenderer re = c.createRenderer();
        final FitnessFunction ff = c.getFitnessFunction(); 
        
        final MersenneTwister rng = new MersenneTwister(v.nextSeed());

        Genome genome = vc.getLatestGenome();
        if (genome == null) {
            genome = gf.createGenome(rng, c);
            if (genome == null) {
                throw new IllegalStateException("GenomeFactory must not return null");
            }
        }
        while (genome != null && !getExecutor().isShutdown()) {
            try {
                final Genome mutated = ms.mutate(rng, c, genome);
                if (mutated == null) {
                    throw new IllegalStateException("MutationStrategy must not return null");
                } else if (mutated == genome) {
                    continue;
                }
                re.render(mutated);
                if (re.getAutoUpdateBuffer()) {
                    mutated.fitness = ff.compute(c, mutated, re.getBuffer());
                } else {
                    mutated.fitness = ff.compute(c, mutated, re.readData());
                }
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
