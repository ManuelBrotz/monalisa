package ch.brotzilla.monalisa.evolution.selectors;

import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.util.MersenneTwister;

public class BasicIndexSelector implements IndexSelector {

    public BasicIndexSelector() {}

    @Override
    public int select(MersenneTwister rng, int length) {
        return rng.nextInt(length);
    }

}
