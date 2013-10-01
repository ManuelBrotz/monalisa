package ch.brotzilla.monalisa.evolution.mutations;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.mutations.intf.Mutation;

public abstract class BasicMutation implements Mutation {

    private final String id, name, description;
    
    public BasicMutation(String id, String name, String description) {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        Preconditions.checkArgument(!id.trim().isEmpty(), "The parameter 'id' must not be empty");
        this.id = id;
        this.name = (name == null) ? "" : name;
        this.description = (description == null) ? "" : description;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    
    
}