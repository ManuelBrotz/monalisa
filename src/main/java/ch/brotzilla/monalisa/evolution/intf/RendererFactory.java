package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public interface RendererFactory {

    Renderer createRenderer(VectorizerConfig config);
    
}
