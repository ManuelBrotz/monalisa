package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.mutations.GeneAddPointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneAlphaChannelMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneColorBrighterMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneColorChannelMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneColorDarkerMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneDilateMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenePointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneRemovePointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneSwapPointsMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenomeAddGeneMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenomeRemoveGeneMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenomeSwapGenesMutation;
import ch.brotzilla.monalisa.evolution.selectors.BasicIndexSelector;
import ch.brotzilla.monalisa.evolution.selectors.BasicTableSelector;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class StaticMutationStrategy implements MutationStrategy {
    
    protected static final IndexSelector defaultMutationSelector = new BasicIndexSelector();
    
    protected static final GenePointMutation geneMovePoint = new GenePointMutation();
    protected static final GeneDilateMutation geneDilate = new GeneDilateMutation();
    protected static final GeneAddPointMutation geneAddPoint = new GeneAddPointMutation();
    protected static final GeneRemovePointMutation geneRemovePoint = new GeneRemovePointMutation();
    protected static final GeneSwapPointsMutation geneSwapPoints = new GeneSwapPointsMutation();
    protected static final GeneAlphaChannelMutation geneAlphaChannel = new GeneAlphaChannelMutation();
    protected static final GeneColorChannelMutation geneColorChannel = new GeneColorChannelMutation();
    protected static final GeneColorBrighterMutation geneBrighterColor = new GeneColorBrighterMutation();
    protected static final GeneColorDarkerMutation geneDarkerColor = new GeneColorDarkerMutation();

    protected static final BasicTableSelector<GeneMutation> geneImportantMutations = 
            new BasicTableSelector<GeneMutation>(defaultMutationSelector, geneMovePoint);
    
    protected static final BasicTableSelector<GeneMutation> geneColorMutations = 
            new BasicTableSelector<GeneMutation>(defaultMutationSelector, geneAlphaChannel, geneColorChannel, geneBrighterColor, geneDarkerColor);

    protected static final BasicTableSelector<GeneMutation> geneRareMutations = 
            new BasicTableSelector<GeneMutation>(defaultMutationSelector, geneAddPoint, geneRemovePoint, geneSwapPoints);
        
    protected static final GenomeAddGeneMutation genomeAddGene = new GenomeAddGeneMutation();
    protected static final GenomeRemoveGeneMutation genomeRemoveGene = new GenomeRemoveGeneMutation();
    protected static final GenomeSwapGenesMutation genomeSwapGenes = new GenomeSwapGenesMutation();

    protected static final BasicTableSelector<GenomeMutation> genomeMutations = 
            new BasicTableSelector<GenomeMutation>(defaultMutationSelector, genomeSwapGenes);

    protected Gene mutateGene(MersenneTwister rng, VectorizerConfig config, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final float p = rng.nextFloat();
        if (p < 0.75f) {
            return geneImportantMutations.select(rng).apply(rng, config, input);
        }
        if (p < 0.95f) {
            return geneColorMutations.select(rng).apply(rng, config, input);
        }
        return geneRareMutations.select(rng).apply(rng, config, input);
    }
    
    protected Genome mutateGene(MersenneTwister rng, VectorizerConfig config, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene[] genes = input.genes;
        final int index = config.getEvolutionContext().getGeneIndexSelector().select(rng, genes.length);
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
        Genome mutated = genomeMutations.select(rng).apply(rng, config, input);
        if (mutated == null || mutated == input || !config.getConstraints().satisfied(config, mutated)) {
            return input;
        }
        return mutated;
    }
    
    public StaticMutationStrategy() {
    }
    
    @Override
    public Genome mutate(MersenneTwister rng, VectorizerConfig config, final Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final int count = 1 + rng.nextInt(2);
        Genome result = input;
        for (int i = 0; i < count; i++) {
            Genome mutated = result;
            while (mutated == result) {
                if (rng.nextBoolean(0.99f)) {
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