package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class GenomeAddGeneMutation extends BasicMutation implements GenomeMutation {

    public GenomeAddGeneMutation() {
        super("add-gene", "Add Gene", "Adds a random gene to the passed genome");
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input) {
        return Utils.appendGene(input, rng, config, null);
    }

}
