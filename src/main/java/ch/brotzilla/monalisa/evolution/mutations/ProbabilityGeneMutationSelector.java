package ch.brotzilla.monalisa.evolution.mutations;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class ProbabilityGeneMutationSelector extends AbstractGeneMutationSelector {

    protected final double[] probabilities; 
    
    protected ProbabilityGeneMutationSelector(Builder builder) {
        super(builder);
        this.probabilities = builder.buildProbabilities();
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerConfig config, Gene input) {
        if (length == 1) {
            return mutations[0].apply(rng, config, input);
        }
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        final double selector = rng.nextDouble();
        double lower = 0.0d;
        for (int i = 0; i < length; i++) {
            final double upper = lower + probabilities[i];
            if (selector > lower && selector <= upper) {
                return mutations[i].apply(rng, config, input);
            }
            lower = upper;
        }
        return mutations[length - 1].apply(rng, config, input);
    }
    
    public static class Builder extends AbstractBuilder {
        
        protected final List<Double> probabilities;
        
        protected double[] buildProbabilities() {
            final double sum = sum();
            final int length = size();
            final double[] result = new double[length];
            for (int i = 0; i < length; i++) {
                result[i] = probabilities.get(i) / sum;
            }
            return result;
        }
        
        protected double sum() {
            double sum = 0;
            for (final Double p : probabilities) {
                Preconditions.checkNotNull(p, "Null entries are not allowed");
                sum += p;
            }
            return sum;
        }

        public Builder(String id, String name, String description) {
            super(id, name, description);
            this.probabilities = Lists.newArrayList();
        }

        public Builder() {
            this("probability-gene-mutation-selector", "ProbabilityGeneMutationSelector", "");
        }
        
        @Override
        public Builder checkReady() {
            Preconditions.checkState(!probabilities.isEmpty(), "The list of probabilities must not be empty");
            Preconditions.checkState(probabilities.indexOf(null) == -1, "The list of probabilities must not contain null elements");
            Preconditions.checkState(probabilities.size() == size(), "The number of probabilities has to be equal to the number of mutations");
            super.checkReady();
            return this;
        }
        
        @Override
        public boolean isReady() {
            return !probabilities.isEmpty() && probabilities.size() == size() && super.isReady();
        }

        public Builder setID(String value){
            return (Builder) super.setID(value);
        }
        
        public Builder setName(String value) {
            return (Builder) super.setName(value);
        }
        
        public Builder setDescription(String value) {
            return (Builder) super.setDescription(value);
        }
        
        public Builder clear() {
            return (Builder) super.clear();
        }
        
        public Builder add(double probability, GeneMutation... mutations) {
            Preconditions.checkArgument(probability > 0.0d, "The parameter 'probability' has to be greater than zero");
            Preconditions.checkNotNull(mutations, "The parameter 'mutations' must not be null");
            Preconditions.checkArgument(mutations.length > 0, "The length of the parameter 'mutations' has to be greater than zero");
            if (mutations.length == 1) {
                super.add(mutations[0]);
            } else {
                super.add(new RandomGeneMutationSelector.Builder().add(mutations).build());
            }
            probabilities.add(probability);
            return this;
        }
        
        @Override
        public GeneMutation build() {
            return new ProbabilityGeneMutationSelector(this);
        }

    }

}
