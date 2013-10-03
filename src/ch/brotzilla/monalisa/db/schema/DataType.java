package ch.brotzilla.monalisa.db.schema;

import com.google.common.base.Preconditions;

public enum DataType {
    
    Null("NULL"),
    Integer("INTEGER"),
    Real("REAL"), 
    Text("TEXT"),
    Blob("BLOB");
    
    public final String name;

    @Override
    public String toString() {
        return name;
    }
    
    private DataType(String name) {
        Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
        Preconditions.checkArgument(name.equals(name.trim()), "The parameter 'name' must not contain whitespace");
        Preconditions.checkArgument(!name.isEmpty(), "The parameter 'name' must not be empty");
        this.name = name;
    }
}