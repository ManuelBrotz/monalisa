package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.filters.LayeredStrategyFilter;
import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.GenomeFilter;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.intf.RendererFactory;
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
import ch.brotzilla.monalisa.rendering.LayeredRenderer;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class LayeredEvolutionStrategy implements EvolutionStrategy {
    
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

    protected static final BasicTableSelector<GeneMutation> geneRareMutations = 
            new BasicTableSelector<GeneMutation>(defaultMutationSelector, geneAddPoint, geneRemovePoint, geneSwapPoints);
        
    protected static final GenomeAddGeneMutation genomeAddGene = new GenomeAddGeneMutation();
    protected static final GenomeRemoveGeneMutation genomeRemoveGene = new GenomeRemoveGeneMutation();
    protected static final GenomeSwapGenesMutation genomeSwapGenes = new GenomeSwapGenesMutation();

    protected static final BasicTableSelector<GenomeMutation> genomeMutations = 
            new BasicTableSelector<GenomeMutation>(defaultMutationSelector, /* genomeAddGene, genomeRemoveGene, */ genomeSwapGenes);

    protected static final RendererFactory rendererFactory = new RendererFactory() {
        @Override
        public Renderer createRenderer(VectorizerContext vc, EvolutionContext ec) {
            return new LayeredRenderer(vc.getWidth(), vc.getHeight(), true);
        }
    };
    protected static final GenomeFactory genomeFactory = new SimpleGenomeFactory(5, 5);
    protected static final GenomeFilter genomeFilter = new LayeredStrategyFilter(rendererFactory);
    
    protected Gene mutateGene(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Gene input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(vectorizerContext, "The parameter 'vectorizerContext' must not be null");
        Preconditions.checkNotNull(evolutionContext, "The parameter 'evolutionContext' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final float p = rng.nextFloat();
        if (p < 0.75f) {
            return geneImportantMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
        }
        if (p < 0.95f) {
            return geneColorMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
        }
        return geneRareMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
    }
    
    protected Genome mutateGene(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(vectorizerContext, "The parameter 'vectorizerContext' must not be null");
        Preconditions.checkNotNull(evolutionContext, "The parameter 'evolutionContext' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final Gene[] layer = input.getCurrentLayer();
        final int index = evolutionContext.getGeneIndexSelector().select(rng, layer.length);
        final Gene selected = layer[index];
        final Gene mutated  = mutateGene(rng, vectorizerContext, evolutionContext, selected);
        if (mutated == null || mutated == selected || !Utils.hasAcceptableAngles(mutated, 15.0d) || Utils.isSelfIntersecting(mutated)) {
            return input;
        }
        final Genome result = new Genome(input);
        result.getCurrentLayer()[index] = mutated; 
        return result;
    }
    
    protected Genome mutateGenome(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, final Genome input) {
        return genomeMutations.select(rng).apply(rng, vectorizerContext, evolutionContext, input);
    }
    
    public LayeredEvolutionStrategy() {}
    
    @Override
    public RendererFactory getRendererFactory() {
        return rendererFactory;
    }
    
    @Override
    public GenomeFactory getGenomeFactory() {
        return genomeFactory;
    }
    
    @Override
    public GenomeFilter getGenomeFilter() {
        return genomeFilter;
    }
    
    @Override
    public Genome mutate(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, final Genome input) {
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        Preconditions.checkNotNull(vectorizerContext, "The parameter 'vectorizerContext' must not be null");
        Preconditions.checkNotNull(evolutionContext, "The parameter 'evolutionContext' must not be null");
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        final int count = 1 + rng.nextInt(2);
        Genome result = input;
        for (int i = 0; i < count; i++) {
            Genome mutated = result;
            while (mutated == result) {
                if (rng.nextBoolean(0.99f)) {
                    mutated = mutateGene(rng, vectorizerContext, evolutionContext, result);
                } else {
                    mutated = mutateGenome(rng, vectorizerContext, evolutionContext, result);
                }
            }
            result = mutated;
        }
        return result;
    }
}