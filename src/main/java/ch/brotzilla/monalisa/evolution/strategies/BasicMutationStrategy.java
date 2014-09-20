package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class BasicMutationStrategy implements MutationStrategy {

    private final GeneMutation geneMutation;
    private final GenomeMutation genomeMutation;
    
    protected Gene mutateGene(MersenneTwister rng, VectorizerConfig config, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        return geneMutation.apply(rng, config, input);
    }
    
    protected Genome mutateGene(MersenneTwister rng, VectorizerConfig config, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene[] genes = input.genes;
        final int index = config.getMutationConfig().getGeneIndexSelector().select(rng, genes.length);
        final Gene selected = genes[index];
        final Gene mutated  = mutateGene(rng, config, selected);
        if (mutated == null || mutated == selected || !config.getConstraints().satisfied(config, mutated)) {
            return input;
        }
        final Genome result = new Genome(input);
        result.genes[index] = mutated; 
        return result;
    }
    
    protected Genome mutateGenome(MersenneTwister rng, VectorizerConfig config, final Genome input) {
        Genome mutated = genomeMutation.apply(rng, config, input);
        if (mutated == null || mutated == input || !config.getConstraints().satisfied(config, mutated)) {
            return input;
        }
        return mutated;
    }

    public BasicMutationStrategy(GeneMutation geneMutation, GenomeMutation genomeMutation) {
        Preconditions.checkNotNull(geneMutation, "The parameter 'geneMutation' must not be null");
        Preconditions.checkNotNull(genomeMutation, "The parameter 'genomeMutation' must not be null");
        this.geneMutation = geneMutation;
        this.genomeMutation = genomeMutation;
    }
    
    public GeneMutation getGeneMutation() {
        return geneMutation;
    }
    
    public GenomeMutation getGenomeMutation() {
        return genomeMutation;
    }

    @Override
    public Genome mutate(MersenneTwister rng, VectorizerConfig config, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final MutationConfig mc = config.getMutationConfig();
        final int min = mc.getMinMutationsPerGenome(), max = mc.getMaxMutationsPerGenome();
        final int count = (min == max) ? min : min + rng.nextInt(max - min + 1);
        Preconditions.checkState(count > 0, "Number of mutations must be greater than zero");
        Genome result = input;
        for (int i = 0; i < count; i++) {
            Genome mutated = result;
            while (mutated == result) {
                if (rng.nextBoolean(mc.getGeneVersusGenomeMutationProbability())) {
                    mutated = mutateGene(rng, config, result);
                } else {
                    mutated = mutateGenome(rng, config, result);
                }
            }
            result = mutated;
        }
        return result;
    }
    
}
