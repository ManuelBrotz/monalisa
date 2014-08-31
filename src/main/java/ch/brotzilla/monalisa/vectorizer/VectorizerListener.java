package ch.brotzilla.monalisa.vectorizer;

import ch.brotzilla.monalisa.evolution.genes.Genome;

public interface VectorizerListener {

    void started(Vectorizer v, Genome latest);
    
    void improved(Vectorizer v, Genome latest);
    
    void update(Vectorizer v);
    
    void stopping(Vectorizer v);
    
    void stopped(Vectorizer v);
    
}
