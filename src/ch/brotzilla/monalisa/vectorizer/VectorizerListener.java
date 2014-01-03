package ch.brotzilla.monalisa.vectorizer;

import ch.brotzilla.monalisa.evolution.genes.Genome;

public interface VectorizerListener {

    void started(Vectorizer v, Genome latest);
    
    void improvement(Vectorizer v, Genome latest);
    
    void stopping(Vectorizer v);
    
    void stopped(Vectorizer v);
    
}
