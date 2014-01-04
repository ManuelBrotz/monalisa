package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.util.MersenneTwister;


public interface TableSelector<T> {

    T select(MersenneTwister rng);
    
}
