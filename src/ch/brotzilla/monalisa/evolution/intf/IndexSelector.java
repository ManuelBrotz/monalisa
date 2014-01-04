package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.util.MersenneTwister;


public interface IndexSelector {

    int select(MersenneTwister rng, int length);
    
}
