package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Constraints;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneSwapPointsMutation extends BasicMutation implements GeneMutation {

    public GeneSwapPointsMutation(double probability) {
        super(probability);
    }

    @Override
    public Gene apply(MersenneTwister rng, Constraints constraints, Gene input) {
        final Gene result = new Gene(input);
        final int len = input.x.length;
        final int index1 = rng.nextInt(len);
        int index2 = rng.nextInt(len);
        while (index1 == index2) {
            index2 = rng.nextInt(len);
        }
        int tmpx = result.x[index1];
        int tmpy = result.y[index1];
        result.x[index1] = result.x[index2];
        result.y[index1] = result.y[index2];
        result.x[index2] = tmpx;
        result.y[index2] = tmpy;
        return result;
    }
    
}