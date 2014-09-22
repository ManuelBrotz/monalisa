package ch.brotzilla.monalisa.vectorizer;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.constraints.MutationConstraints;
import ch.brotzilla.monalisa.evolution.intf.EvolutionStrategy;
import ch.brotzilla.monalisa.evolution.intf.FitnessFunction;
import ch.brotzilla.monalisa.evolution.intf.GenomeFactory;
import ch.brotzilla.monalisa.evolution.intf.MutationStrategy;
import ch.brotzilla.monalisa.evolution.intf.RendererFactory;
import ch.brotzilla.monalisa.evolution.strategies.MutationConfig;
import ch.brotzilla.monalisa.rendering.Renderer;

public class VectorizerConfig {

    private final VectorizerContext vectorizerContext;
    private final MutationConfig mutationConfig;
    private final EvolutionStrategy evolutionStrategy;
    private final MutationStrategy mutationStrategy;
    private final GenomeFactory genomeFactory;
    private final RendererFactory rendererFactory;
    private final MutationConstraints mutationConstraints;
    private final FitnessFunction fitnessFunction;

    private VectorizerConfig(Builder builder) {
        Preconditions.checkNotNull(builder, "The parameter 'builder' must not be null");
        builder.checkReady();
        this.vectorizerContext = builder.getVectorizerContext();
        this.mutationConfig = builder.getMutationConfig();
        this.evolutionStrategy = builder.getEvolutionStrategy();
        this.mutationStrategy = builder.getMutationStrategy();
        this.genomeFactory = builder.getGenomeFactory();
        this.rendererFactory = builder.getRendererFactory();
        this.mutationConstraints = builder.getConstraints();
        this.fitnessFunction = builder.getFitnessFunction();
    }

    public int getWidth() {
        return vectorizerContext.getWidth();
    }

    public int getHeight() {
        return vectorizerContext.getHeight();
    }

    public VectorizerContext getVectorizerContext() {
        return vectorizerContext;
    }

    public MutationConfig getMutationConfig() {
        return mutationConfig;
    }

    public EvolutionStrategy getEvolutionStrategy() {
        return evolutionStrategy;
    }

    public MutationStrategy getMutationStrategy() {
        return mutationStrategy;
    }

    public GenomeFactory getGenomeFactory() {
        return genomeFactory;
    }

    public RendererFactory getRendererFactory() {
        return rendererFactory;
    }

    public MutationConstraints getConstraints() {
        return mutationConstraints;
    }

    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    public Renderer createRenderer() {
        final Renderer result = getRendererFactory().createRenderer(this);
        Preconditions.checkState(result != null, "The renderer factory must not return null");
        return result;
    }

    public static class Builder implements ch.brotzilla.monalisa.intf.Builder<VectorizerConfig> {

        private VectorizerContext vectorizerContext;
        private MutationConfig mutationConfig;
        private EvolutionStrategy evolutionStrategy;
        private MutationStrategy mutationStrategy;
        private GenomeFactory genomeFactory;
        private RendererFactory rendererFactory;
        private MutationConstraints mutationConstraints;
        private FitnessFunction fitnessFunction;

        @Override
        public Builder checkReady() {
            Preconditions.checkNotNull(getVectorizerContext(), "The property 'VectorizerContext' must not be null");
            Preconditions.checkNotNull(getMutationConfig(), "The property 'MutationConfig' must not be null");
            Preconditions.checkNotNull(getEvolutionStrategy(), "The property 'EvolutionStrategy' must not be null");
            Preconditions.checkNotNull(getMutationStrategy(), "The property 'MutationStrategy' must not be null");
            Preconditions.checkNotNull(getGenomeFactory(), "The property 'GenomeFactory' must not be null");
            Preconditions.checkNotNull(getRendererFactory(), "The property 'RendererFactory' must not be null");
            Preconditions.checkNotNull(getConstraints(), "The property 'Constraints' must not be null");
            Preconditions.checkNotNull(getFitnessFunction(), "The property 'FitnessFunction' must not be null");
            Preconditions.checkState(isReady(), "The vectorizer configuration is not ready");
            return this;
        }

        @Override
        public boolean isReady() {
            return vectorizerContext != null && mutationConfig != null && mutationStrategy != null && genomeFactory != null && rendererFactory != null
                    && mutationConstraints != null && fitnessFunction != null;
        }

        public VectorizerContext getVectorizerContext() {
            return vectorizerContext;
        }

        public Builder setVectorizerContext(VectorizerContext value) {
            this.vectorizerContext = value;
            return this;
        }

        public MutationConfig getMutationConfig() {
            return mutationConfig;
        }

        public Builder setMutationConfig(MutationConfig value) {
            this.mutationConfig = value;
            return this;
        }

        public EvolutionStrategy getEvolutionStrategy() {
            return evolutionStrategy;
        }

        public Builder setEvolutionStrategy(EvolutionStrategy value) {
            this.evolutionStrategy = value;
            return this;
        }

        public MutationStrategy getMutationStrategy() {
            return mutationStrategy;
        }

        public Builder setMutationStrategy(MutationStrategy value) {
            this.mutationStrategy = value;
            return this;
        }

        public GenomeFactory getGenomeFactory() {
            return genomeFactory;
        }

        public Builder setGenomeFactory(GenomeFactory value) {
            this.genomeFactory = value;
            return this;
        }

        public RendererFactory getRendererFactory() {
            return rendererFactory;
        }

        public Builder setRendererFactory(RendererFactory value) {
            this.rendererFactory = value;
            return this;
        }

        public MutationConstraints getConstraints() {
            return mutationConstraints;
        }

        public Builder setConstraints(MutationConstraints value) {
            this.mutationConstraints = value;
            return this;
        }

        public FitnessFunction getFitnessFunction() {
            return fitnessFunction;
        }

        public Builder setFitnessFunction(FitnessFunction value) {
            this.fitnessFunction = value;
            return this;
        }

        @Override
        public VectorizerConfig build() {
            return new VectorizerConfig(this);
        }

    }

}
