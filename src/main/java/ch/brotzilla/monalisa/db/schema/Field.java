package ch.brotzilla.monalisa.db.schema;

import com.google.common.base.Preconditions;

public final class Field implements Item {
    
    private final DataType type;
    private final String name, description;
    private final boolean isNullable, isPrimaryKey;
    
    public Field(String name, DataType type, boolean isNullable, boolean isPrimaryKey) {
        Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
        Preconditions.checkArgument(name.equals(name.trim()), "The parameter 'name' must not contain whitespace");
        Preconditions.checkArgument(!name.isEmpty(), "The parameter 'name' must not be empty");
        this.name = name;
        
        Preconditions.checkNotNull(type, "The parameter 'type' must not be null");
        this.type = type;
        
        this.isNullable = isNullable;
        this.isPrimaryKey = isPrimaryKey;

        this.description = name + " " + type + (!isNullable ? " NOT NULL" : "") + (isPrimaryKey ? " PRIMARY KEY" : "");
    }

    public final DataType getType() {
        return type;
    }
    
    @Override
    public final String getName() {
        return name;
    }
    
    public final String getDescription() {
        return description;
    }
    
    public final boolean isNullable() {
        return isNullable;
    }
    
    public final boolean isPrimaryKey() {
        return isPrimaryKey;
    }
    
    @Override
    public final String toString() {
        return name;
    }
}