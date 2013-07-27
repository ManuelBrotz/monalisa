package ch.brotzilla.monalisa.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.mutations.intf.GenomeMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;
import ch.brotzilla.monalisa.utils.Utils;

public class GenomeAddGeneMutation extends BasicMutation implements GenomeMutation {

    protected final int[] inputData;
    
    public GenomeAddGeneMutation(int[] inputData, double probability) {
        super(probability);
        this.inputData = Preconditions.checkNotNull(inputData, "The parameter 'inputData' must not be null");
    }

    @Override
    public Genome apply(MersenneTwister rng, Constraints constraints, Genome input) {
        final Gene gene = Utils.createRandomGene(rng, constraints, inputData);
        final Gene[] genes = new Gene[input.genes.length + 1];
        System.arraycopy(input.genes, 0, genes, 0, input.genes.length);
        genes[genes.length - 1] = gene;
        return new Genome(input.background, genes);
    }

}
