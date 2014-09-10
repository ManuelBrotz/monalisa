package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class GenomeAddGeneMutation extends BasicMutation implements GenomeMutation {

    protected final GenomeFactory genomeFactory;
    
    public GenomeAddGeneMutation(GenomeFactory genomeFactory) {
        super("add-gene", "Add Gene", "Adds a random gene to the passed genome");
        Preconditions.checkNotNull(genomeFactory, "The parameter 'genomeFactory' must not be null");
        this.genomeFactory = genomeFactory;
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        return Utils.appendGene(input, rng, vectorizerContext, evolutionContext, genomeFactory);
    }

}
