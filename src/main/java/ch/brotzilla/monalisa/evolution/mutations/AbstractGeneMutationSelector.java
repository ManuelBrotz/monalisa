package ch.brotzilla.monalisa.evolution.mutations;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.intf.GeneMutation;

public abstract class AbstractGeneMutationSelector extends BasicMutation implements GeneMutation {

    protected final GeneMutation[] mutations;
    protected final int length;
    
    protected AbstractGeneMutationSelector(String id, String name, String description, GeneMutation[] mutations) {
        super(id, name, description);
        Preconditions.checkNotNull(mutations, "The parameter 'mutations' must not be null");
        Preconditions.checkArgument(mutations.length > 0, "The length of the parameter 'mutations' has to be greater than zero");
        this.mutations = mutations;
        this.length = mutations.length;
    }

    protected abstract static class AbstractBuilder {
        
        private final List<GeneMutation> mutations;
        private String id, name, description;
        
        protected abstract GeneMutation buildSelector(GeneMutation[] mutations);
        
        protected AbstractBuilder(String id, String name, String description) {
            this.mutations = Lists.newArrayList();
            this.id = id;
            this.name = name;
            this.description = description;
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
        
        public GeneMutation build() {
            final int length = mutations.size();
            if (length == 0) {
                throw new IllegalStateException("The builder must not be empty");
            }
            final GeneMutation[] tmp = new GeneMutation[length];
            for (int i = 0; i < length; i++) {
                tmp[i] = mutations.get(i);
            }
            return buildSelector(tmp);
        }
    }

}
