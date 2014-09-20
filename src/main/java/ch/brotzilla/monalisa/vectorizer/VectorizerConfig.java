package ch.brotzilla.monalisa.vectorizer;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.constraints.MutationConstraints;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.intf.FitnessFunction;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.intf.RendererFactory;
import ch.brotzilla.monalisa.evolution.strategies.MutationConfig;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.Renderer;

public class VectorizerConfig {

    private int width, height;
    private VectorizerContext vectorizerContext;
    private MutationConfig mutationConfig;
    private EvolutionStrategy evolutionStrategy;
    private MutationStrategy mutationStrategy;
    private GenomeFactory genomeFactory;
    private RendererFactory rendererFactory;
    private MutationConstraints mutationConstraints;
    private FitnessFunction fitnessFunction;

    boolean frozen = false;
    
    public VectorizerConfig() {
    }

    public boolean isReady() {
        return width > 0 && height > 0 
                && vectorizerContext != null 
                && mutationConfig != null 
                && mutationStrategy != null 
                && genomeFactory != null
                && rendererFactory != null
                && mutationConstraints != null
                && fitnessFunction != null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void setSize(int width, int height) {
        checkFrozenProperty("Size");
        Preconditions.checkArgument(width >= 0, "The parameter 'width' has to be greater than or equal to zero");
        Preconditions.checkArgument(height >= 0, "The parameter 'height' has to be greater than or equal to zero");
        this.width = width;
        this.height = height;
    }

    public VectorizerContext getVectorizerContext() {
        return vectorizerContext;
    }
    
    public void setVectorizerContext(VectorizerContext value) {
        checkFrozenProperty("VectorizerContext");
        this.vectorizerContext = value;
    }

    public void setSession(SessionManager session) {
        checkFrozenProperty("Session");
        if (session != null) {
            this.width = session.getWidth();
            this.height = session.getHeight();
            this.vectorizerContext = session.getVectorizerContext();
        } else {
            this.width = 0;
            this.height = 0;
            this.vectorizerContext = null;
        }
    }
    
    public MutationConfig getMutationConfig() {
        return mutationConfig;
    }

    public void setMutationConfig(MutationConfig value) {
        checkFrozenProperty("MutationConfig");
        this.mutationConfig = value;
    }

    public EvolutionStrategy getEvolutionStrategy() {
        return evolutionStrategy;
    }

    public void setEvolutionStrategy(EvolutionStrategy value) {
        checkFrozenProperty("EvolutionStrategy");
        this.evolutionStrategy = value;
    }

    public MutationStrategy getMutationStrategy() {
        return mutationStrategy;
    }

    public void setMutationStrategy(MutationStrategy value) {
        checkFrozenProperty("MutationStrategy");
        this.mutationStrategy = value;
    }

    public GenomeFactory getGenomeFactory() {
        return genomeFactory;
    }

    public void setGenomeFactory(GenomeFactory value) {
        checkFrozenProperty("GenomeFactory");
        this.genomeFactory = value;
    }

    public RendererFactory getRendererFactory() {
        return rendererFactory;
    }
    
    public Renderer createRenderer() {
        checkReady();
        final Renderer result = getRendererFactory().createRenderer(this);
        Preconditions.checkState(result != null, "The renderer factory must not return null");
        return result;
    }

    public void setRendererFactory(RendererFactory value) {
        checkFrozenProperty("RendererFactory");
        this.rendererFactory = value;
    }
    
    public MutationConstraints getConstraints() {
        return mutationConstraints;
    }
    
    public void setConstraints(MutationConstraints value) {
        checkFrozenProperty("Constraints");
        this.mutationConstraints = value;
    }
    
    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }
    
    public void setFitnessFunction(FitnessFunction value) {
        checkFrozenProperty("FitnessFunction");
        this.fitnessFunction = value;
    }

    public void checkReady() {
        if (!isReady()) {
            throw new IllegalStateException("The vectorizer configuration is not ready");
        }
    }

    private void checkFrozenProperty(String property) {
        if (frozen) {
            throw new IllegalStateException("Property '" + property + "' cannot be changed while the vectorizer is running");
        }
    }

}