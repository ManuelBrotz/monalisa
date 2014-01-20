package ch.brotzilla.monalisa.db.schema;

import com.google.common.base.Preconditions;

public class Table implements Item {
    
    private final Fields fields;
    private final String name, createTableQuery, createTableIfNotExistsQuery;
    
    public Table(String name, Field... fields) {
        this(name, new Fields(fields));
    }
    
    public Table(String name, Fields fields) {
        Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
        Preconditions.checkArgument(name.equals(name.trim()), "The parameter 'name' must not contain whitespace");
        Preconditions.checkArgument(!name.isEmpty(), "The parameter 'name' must not be empty");
        this.name = name;
        
        Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
        this.fields = fields;
        
        this.createTableQuery = Schema.CreateTable + " " + name + " (" + fields.getFieldDescriptions() + ")";
        this.createTableIfNotExistsQuery = Schema.CreateTableIfNotExists + " " + name + " (" + fields.getFieldDescriptions() + ")";
    }
    
    public final Fields getFields() {
        return fields;
    }

    @Override
    public final String getName() {
        return name;
    }
    
    public final String getCreateTableQuery() {
        return createTableQuery;
    }
    
    public final String getCreateTableIfNotExistsQuery() {
        return createTableIfNotExistsQuery;
    }
    
    @Override
    public final String toString() {
        return name;
    }
}