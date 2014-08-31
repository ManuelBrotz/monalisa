package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;

public interface GenomeFilter {
    
    Genome apply(Genome genome);
    
}
