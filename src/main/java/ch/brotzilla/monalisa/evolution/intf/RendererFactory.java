package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.rendering.GenomeRenderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public interface RendererFactory {

    GenomeRenderer createRenderer(VectorizerConfig config);
    
}
