package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.mutations.intf.IndexSelector;
import ch.brotzilla.monalisa.mutations.intf.GenomeMutation;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.utils.Utils;

public class GenomeAddGeneMutation extends BasicMutation implements GenomeMutation {

    public GenomeAddGeneMutation() {
        super("add-gene", "Add Gene", "Adds a random gene to the passed genome");
    }

    @Override
    public Genome apply(MersenneTwister rng, IndexSelector selector, Context context, Genome input) {
        final Gene gene = Utils.createRandomGene(rng, context);
        final Gene[] genes = new Gene[input.genes.length + 1];
        System.arraycopy(input.genes, 0, genes, 0, input.genes.length);
        genes[genes.length - 1] = gene;
        return new Genome(input.background, genes);
    }

}
