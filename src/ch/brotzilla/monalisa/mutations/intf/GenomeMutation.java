package ch.brotzilla.monalisa.mutations.intf;

import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface GenomeMutation extends Mutation {

    public Genome apply(MersenneTwister rng, Constraints constraints, Genome input);
    
}
