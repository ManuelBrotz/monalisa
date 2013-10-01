package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.utils.MersenneTwister;

public interface TableSelector<T> {

    T select(MersenneTwister rng);
    
}
