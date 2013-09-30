package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.genes.Gene;
import ch.brotzilla.monalisa.mutations.intf.GeneMutation;
import ch.brotzilla.monalisa.utils.Context;
import ch.brotzilla.monalisa.utils.MersenneTwister;

public class GeneRemovePointMutation extends BasicMutation implements GeneMutation {

    public GeneRemovePointMutation() {
        super("remove-point", "Remove Point", "Removes a randomly chosen point of the passed gene");
    }

    @Override
    public Gene apply(MersenneTwister rng, Context context, Gene input) {
        if (input.x.length > 3) {
            final int length = input.x.length;
            final int index = rng.nextInt(length);
            final int[] x = new int[length - 1], y = new int[length - 1];
            if (index == 0) {
                System.arraycopy(input.x, 1, x, 0, length - 1);
                System.arraycopy(input.y, 1, y, 0, length - 1);
            } else if (index == length - 1) {
                System.arraycopy(input.x, 0, x, 0, length - 1);
                System.arraycopy(input.y, 0, y, 0, length - 1);
            } else {
                System.arraycopy(input.x, 0, x, 0, index);
                System.arraycopy(input.x, index + 1, x, index, length - index - 1);
                System.arraycopy(input.y, 0, y, 0, index);
                System.arraycopy(input.y, index + 1, y, index, length - index - 1);
            }
            return new Gene(x, y, input.color);
        }
        return input;
    }
    
}