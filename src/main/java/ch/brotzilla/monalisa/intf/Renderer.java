package ch.brotzilla.monalisa.intf;

import ch.brotzilla.monalisa.evolution.genes.Genome;

public interface Renderer {

    void render(Genome genome);
    
    int[] getImageData();
    
}
