package ch.brotzilla.monalisa.evolution.mutations;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.intf.GeneMutation;
import ch.brotzilla.monalisa.intf.Builder;

public abstract class AbstractGeneMutationSelector extends BasicMutation implements GeneMutation {

    protected final GeneMutation[] mutations;
    protected final int length;
    
    protected AbstractGeneMutationSelector(AbstractBuilder builder) {
        super(Preconditions.checkNotNull(builder, "The parameter 'builder' must not be null").checkReady().getID(), builder.getName(), builder.getDescription());
        this.mutations = builder.buildMutations();
        this.length = mutations.length;
    }

    protected abstract static class AbstractBuilder implements Builder<GeneMutation> {
        
        private final List<GeneMutation> mutations;
        private String id, name, description;
        
        protected GeneMutation[] buildMutations() {
            final int length = mutations.size();
            final GeneMutation[] result = new GeneMutation[length];
            for (int i = 0; i < length; i++) {
                result[i] = mutations.get(i);
            }
            return result;
        }
        
        protected AbstractBuilder(String id, String name, String description) {
            this.mutations = Lists.newArrayList();
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        @Override
        public AbstractBuilder checkReady() {
            Preconditions.checkState(!mutations.isEmpty(), "The list of mutations must not be empty");
            Preconditions.checkState(mutations.indexOf(null) == -1, "The list of mutations must not contain null elements");
            Preconditions.checkState(isReady(), "The mutation selector is not ready");
            return this;
        }
        
        @Override
        public boolean isReady() {
            return !mutations.isEmpty() && mutations.indexOf(null) == -1;
        }

        public String getID() {
            return id;
        }
        
        protected AbstractBuilder setID(String value){
            id = value;
            return this;
        }
        
        public String getName() {
            return name;
        }
        
        protected AbstractBuilder setName(String value) {
            name = value;
            return this;
        }
        
        public String getDescription() {
            return description;
        }
        
        protected AbstractBuilder setDescription(String value) {
            description = value;
            return this;
        }
        
        public int size() {
            return mutations.size();
        }
        
        public GeneMutation get(int index) {
            return mutations.get(index);
        }
        
        protected AbstractBuilder clear() {
            mutations.clear();
            return this;
        }
        
        protected AbstractBuilder add(GeneMutation... mutations) {
            Preconditions.checkNotNull(mutations, "The parameter 'mutations' must not be null");
            Preconditions.checkArgument(mutations.length > 0, "The length of the parameter 'mutations' has to be greater than zero");
            for (final GeneMutation mutation : mutations) {
                Preconditions.checkNotNull(mutation, "The parameter 'mutations' must not contain null");
                this.mutations.add(mutation);
            }
            return this;
        }
        
    }

}
