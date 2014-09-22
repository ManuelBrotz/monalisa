package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.GenomeMutation;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class RandomGenomeMutationSelector extends AbstractGenomeMutationSelector {

    protected RandomGenomeMutationSelector(Builder builder) {
        super(builder);
    }

    @Override
    public Genome apply(MersenneTwister rng, VectorizerConfig config, Genome input) {
        if (length == 1) {
            return mutations[0].apply(rng, config, input);
        }
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        return mutations[rng.nextInt(length)].apply(rng, config, input);
    }
    
    public static class Builder extends AbstractBuilder {
        
        public Builder(String id, String name, String description) {
            super(id, name, description);
        }

        public Builder() {
            super("random-genome-mutation-selector", "RandomGenomeMutationSelector", "");
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
        
        public Builder add(GenomeMutation... mutations) {
            return (Builder) super.add(mutations);
        }

        @Override
        public GenomeMutation build() {
            return new RandomGenomeMutationSelector(this);
        }

    }

}
