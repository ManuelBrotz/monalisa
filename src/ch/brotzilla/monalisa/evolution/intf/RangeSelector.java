package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface RangeSelector {

    int getMin();
    
    int getMax();
    
    int select(MersenneTwister rng);
    
}
