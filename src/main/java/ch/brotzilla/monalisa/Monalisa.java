package ch.brotzilla.monalisa;

import ch.brotzilla.monalisa.vectorizer.Vectorizer;

public class Monalisa {

    private final Vectorizer vectorizer;
    
    public Monalisa() {
        this.vectorizer = new Vectorizer();
    }
    
    public Vectorizer getVectorizer() {
        return vectorizer;
    }
    
    public void shutdown() {
        
    }

}
