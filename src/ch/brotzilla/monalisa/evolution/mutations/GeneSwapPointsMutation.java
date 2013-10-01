package ch.brotzilla.monalisa.evolution.mutations;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneSwapPointsMutation extends BasicMutation implements GeneMutation {

    public GeneSwapPointsMutation() {
        super("swap-points", "Swap Points", "Swaps two randomly chosen points of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Context context, Gene input) {
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