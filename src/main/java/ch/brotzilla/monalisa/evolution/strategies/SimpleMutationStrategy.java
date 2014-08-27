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
import ch.brotzilla.monalisa.evolution.mutations.GenePointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneRemovePointMutation;
import ch.brotzilla.monalisa.evolution.mutations.GeneSwapPointsMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenomeAddGeneMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenomeRemoveGeneMutation;
import ch.brotzilla.monalisa.evolution.mutations.GenomeSwapGenesMutation;
import ch.brotzilla.monalisa.evolution.selectors.BasicIndexSelector;
import ch.brotzilla.monalisa.evolution.selectors.BasicTableSelector;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class SimpleMutationStrategy implements MutationStrategy {
    
    protected static final IndexSelector defaultMutationSelector = new BasicIndexSelector();
    
    protected static final GenePointMutation geneMovePoint = new GenePointMutation();
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

    protected static final BasicTableSelector<GeneMutation> geneDefaultMutations = 
            new BasicTableSelector<GeneMutation>(defaultMutationSelector, geneAddPoint, geneRemovePoint, geneSwapPoints);
        
    protected static final GenomeAddGeneMutation genomeAddGene = new GenomeAddGeneMutation();
    protected static final GenomeRemoveGeneMutation genomeRemoveGene = new GenomeRemoveGeneMutation();
    protected static final GenomeSwapGenesMutation genomeSwapGenes = new GenomeSwapGenesMutation();

    protected static final BasicTableSelector<GenomeMutation> genomeMutations = 
            new BasicTableSelector<GenomeMutation>(defaultMutationSelector, genomeAddGene, genomeRemoveGene, genomeSwapGenes);

    protected Gene mutateGene(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(vectorizerContext, "The parameter 'vectorizerContext' must not be null");
        Preconditions.checkNotNull(evolutionContext, "The parameter 'evolutionContext' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        if (rng.nextBoolean(0.75f)) {
            return geneImportantMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
        }
        if (rng.nextBoolean(0.25f)) {
            return geneColorMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
        }
        return geneDefaultMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
    }
    
    protected Genome mutateGene(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(vectorizerContext, "The parameter 'vectorizerContext' must not be null");
        Preconditions.checkNotNull(evolutionContext, "The parameter 'evolutionContext' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene[] layer = input.getCurrentLayer();
        final int index = evolutionContext.getGeneIndexSelector().select(rng, layer.length);
        final Gene selected = layer[index];
        Gene mutated = selected;
        while (mutated == selected) {
            mutated = mutateGene(rng, vectorizerContext, evolutionContext, selected);
        }
        final Genome result = new Genome(input);
        result.getCurrentLayer()[index] = mutated; 
        return result;
    }
    
    protected Genome mutateGenome(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, final Genome input) {
        Genome mutated = input;
        while (mutated == input) {
            mutated = genomeMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
        }
        return mutated;
    }
    
    public SimpleMutationStrategy() {}
    
    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, final Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(vectorizerContext, "The parameter 'vectorizerContext' must not be null");
        Preconditions.checkNotNull(evolutionContext, "The parameter 'evolutionContext' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final int count = 1 + rng.nextInt(2);
        Genome result = input;
        for (int i = 0; i < count; i++) {
            if (rng.nextBoolean(0.95f)) {
                result = mutateGene(rng, vectorizerContext, evolutionContext, result);
            } else {
                result = mutateGenome(rng, vectorizerContext, evolutionContext, result);
            }
        }
        return result;
    }
    
}