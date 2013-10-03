package ch.brotzilla.monalisa.db.schema;

import java.util.Arrays;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public final class Index implements Item {
    
    
    private final Table table;
    private final String[] fields;
    private final String name, createIndexQuery, fieldNames;
    
    public Index(String name, Table table, Field... fields) {
        this(name, table, Fields.extractFieldNames(fields));
    }
    
    public Index(String name, Table table, String... fields) {
        Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
        Preconditions.checkArgument(name.equals(name.trim()), "The parameter 'name' must not contain whitespace");
        Preconditions.checkArgument(!name.isEmpty(), "The parameter 'name' must not be empty");
        this.name = name;
        
        Preconditions.checkNotNull(table, "The parameter 'table' must not be null");
        this.table = table;
        
        Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
        Preconditions.checkArgument(fields.length > 0, "The parameter 'fields' must not be empty");
        this.fields = new String[fields.length];
        
        int i = 0;
        for (final String f : fields) {
            Preconditions.checkNotNull(f, "The parameter 'fields' must not contain null");
            if (!table.getFields().has(f))
                throw new IllegalArgumentException("Field '" + f + "' not found in table '" + table + "'");
            this.fields[i++] = f;
        }
        
        this.fieldNames = Joiner.on(", ").join(fields);   
        this.createIndexQuery = Schema.CreateIndex + " " + name + " ON " + table + " (" + fieldNames + ")";
    }
    
    public final Table getTable() {
        return table;
    }
    
    @Override
    public final String getName() {
        return name;
    }
    
    public final String[] getFields() {
        return Arrays.copyOf(fields, fields.length);
    }
    
    public final String getCreateIndexQuery() {
        return createIndexQuery;
    }
    
    public final String getFieldNames() {
        return fieldNames;
    }

    @Override
    public final String toString() {
        return name;
    }
}