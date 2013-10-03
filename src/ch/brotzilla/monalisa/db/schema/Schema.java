package ch.brotzilla.monalisa.db.schema;

import com.google.common.base.Preconditions;
public class Schema {
    
    public static final String CreateTable = "CREATE TABLE";
    public static final String CreateIndex = "CREATE INDEX";

    public final Tables tables;
    public final Indexes indexes;
    
    public Schema(Tables tables, Indexes indexes) {
        Preconditions.checkNotNull(tables, "The parameter 'tables' must not be null");
        Preconditions.checkNotNull(indexes, "The parameter 'indexes' must not be null");
        this.tables = tables;
        this.indexes = indexes;
    }
}