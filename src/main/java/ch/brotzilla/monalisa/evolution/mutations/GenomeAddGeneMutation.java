package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.utils.Utils;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;
import ch.brotzilla.util.MersenneTwister;

public class GenomeAddGeneMutation extends BasicMutation implements GenomeMutation {

    public GenomeAddGeneMutation() {
        super("add-gene", "Add Gene", "Adds a random gene to the passed genome");
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerContext vectorizerContext, EvolutionContext evolutionContext, Genome input) {
        return Utils.appendGeneToCurrentLayer(input, rng, vectorizerContext, evolutionContext);
    }

}
