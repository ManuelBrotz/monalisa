package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MersenneTwister;

public class RandomGeneMutationSelector extends AbstractGeneMutationSelector {

    protected RandomGeneMutationSelector(String id, String name, String description, GeneMutation[] mutations) {
        super(id, name, description, mutations);
    }

    @Override
    public Gene apply(MersenneTwister rng, VectorizerConfig config, Gene input) {
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
            super("random-gene-mutation-selector", "RandomGeneMutationSelector", "");
        }

        @Override
        protected GeneMutation buildSelector(GeneMutation[] mutations) {
            return new RandomGeneMutationSelector(getID(), getName(), getDescription(), mutations);
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
        
        public Builder add(GeneMutation... mutations) {
            return (Builder) super.add(mutations);
        }

    }

}
