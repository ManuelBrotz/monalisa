package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class GenomeAddGeneMutation extends BasicMutation implements GenomeMutation {

    protected final GenomeFactory genomeFactory;
    
    public GenomeAddGeneMutation(GenomeFactory genomeFactory) {
        super("add-gene", "Add Gene", "Adds a random gene to the passed genome");
        Preconditions.checkNotNull(genomeFactory, "The parameter 'genomeFactory' must not be null");
        this.genomeFactory = genomeFactory;
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input) {
        return Utils.appendGene(input, rng, config, genomeFactory);
    }

}
