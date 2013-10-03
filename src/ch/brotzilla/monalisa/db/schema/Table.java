package ch.brotzilla.monalisa.db.schema;

import com.google.common.base.Preconditions;

public class Table implements Item {
    
    public final Fields fields;
    public final String name, createTableQuery;
    
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
        
        this.createTableQuery = Schema.CreateTable + " " + name + " (" + fields.fieldDescriptions + ")";
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}