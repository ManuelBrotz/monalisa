package ch.brotzilla.monalisa.mutations.intf;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface GeneMutation extends Mutation {

    public Gene apply(MersenneTwister rng, Constraints constraints, Gene input);
    
}