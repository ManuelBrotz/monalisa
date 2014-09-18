package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.util.MersenneTwister;


public interface ObjectSelector<T> {

    T select(MersenneTwister rng);
    
}
