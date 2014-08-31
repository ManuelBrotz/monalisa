package ch.brotzilla.monalisa.evolution.intf;

import ch.brotzilla.monalisa.evolution.strategies.EvolutionContext;
import ch.brotzilla.monalisa.rendering.Renderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;

public interface RendererFactory {

    Renderer createRenderer(VectorizerContext vc, EvolutionContext ec);
    
}
