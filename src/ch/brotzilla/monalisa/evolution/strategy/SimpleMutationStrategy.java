package ch.brotzilla.monalisa.evolution.strategy;

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
import ch.brotzilla.monalisa.evolution.selectors.BiasedIndexSelector;
import ch.brotzilla.monalisa.evolution.selectors.ObjectSelector;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class SimpleMutationStrategy implements MutationStrategy {
    
    protected static final GenePointMutation geneMovePoint = new GenePointMutation();
    protected static final GeneAddPointMutation geneAddPoint = new GeneAddPointMutation();
    protected static final GeneRemovePointMutation geneRemovePoint = new GeneRemovePointMutation();
    protected static final GeneSwapPointsMutation geneSwapPoints = new GeneSwapPointsMutation();
    protected static final GeneAlphaChannelMutation geneAlphaChannel = new GeneAlphaChannelMutation();
    protected static final GeneColorChannelMutation geneColorChannel = new GeneColorChannelMutation();
    protected static final GeneColorBrighterMutation geneBrighterColor = new GeneColorBrighterMutation();
    protected static final GeneColorDarkerMutation geneDarkerColor = new GeneColorDarkerMutation();

    protected static final ObjectSelector<GeneMutation> geneImportantMutations = 
            new ObjectSelector<GeneMutation>(geneMovePoint);
    
    protected static final ObjectSelector<GeneMutation> geneColorMutations = 
            new ObjectSelector<GeneMutation>(geneAlphaChannel, geneColorChannel, geneBrighterColor, geneDarkerColor);

    protected static final ObjectSelector<GeneMutation> geneDefaultMutations = 
            new ObjectSelector<GeneMutation>(geneMovePoint);
        
    protected static final GenomeAddGeneMutation genomeAddGene = new GenomeAddGeneMutation();
    protected static final GenomeRemoveGeneMutation genomeRemoveGene = new GenomeRemoveGeneMutation();
    protected static final GenomeSwapGenesMutation genomeSwapGenes = new GenomeSwapGenesMutation();

    protected static final ObjectSelector<GenomeMutation> genomeMutations = 
            new ObjectSelector<GenomeMutation>(genomeAddGene, genomeRemoveGene, genomeSwapGenes);

    protected static final IndexSelector defaultGeneSelector = new BiasedIndexSelector(4);
    
    protected IndexSelector selector;
    
    protected Gene mutateGene(MersenneTwister rng, Context context, Gene input) {
        if (rng.nextBoolean(0.75f)) {
            return geneImportantMutations.select(rng).apply(rng, context, input);
        }
        if (rng.nextBoolean(0.75f)) {
            return geneColorMutations.select(rng).apply(rng, context, input);
        }
        return geneDefaultMutations.select(rng).apply(rng, context, input);
    }
    
    protected Genome mutateGene(MersenneTwister rng, Context context, Genome input) {
        final int index = selector.select(rng, input.genes.length);
        final Gene selected = input.genes[index];
        Gene mutated = selected;
        while (mutated == selected) {
            mutated = mutateGene(rng, context, selected);
        }
        final Genome result = new Genome(input);
        result.genes[index] = mutated; 
        return result;
    }
    
    protected Genome mutateGenome(MersenneTwister rng, Context context, final Genome input) {
        Genome mutated = input;
        while (mutated == input) {
            mutated = genomeMutations.select(rng).apply(rng, selector, context, input);
        }
        return mutated;
    }
    
    public SimpleMutationStrategy() {
        this.selector = defaultGeneSelector;
    }
    
    public IndexSelector getGeneSelector() {
        return selector;
    }
    
    public void setGeneSelector(IndexSelector value) {
        if (value == null) {
            this.selector = defaultGeneSelector;
        } else {
            this.selector = value;
        }
    }

    @Override
    public Genome apply(MersenneTwister rng, Context context, final Genome input) {
        final int count = 1 + rng.nextInt(2);
        Genome result = input;
        for (int i = 0; i < count; i++) {
            if (rng.nextBoolean(0.95f)) {
                result = mutateGene(rng, context, result);
            } else {
                result = mutateGenome(rng, context, result);
            }
        }
        result.mutations = count;
        return result;
    }
    
}