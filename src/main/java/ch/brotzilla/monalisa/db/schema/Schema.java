package ch.brotzilla.monalisa.db.schema;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public abstract class Schema {
    
    public static final String CreateTable = "CREATE TABLE";
    public static final String CreateTableIfNotExists = "CREATE TABLE IF NOT EXISTS";
    public static final String CreateIndex = "CREATE INDEX";

    private final Tables tables;
    private final Indexes indexes;
    
    public Schema(Tables tables, Indexes indexes) {
        Preconditions.checkNotNull(tables, "The parameter 'tables' must not be null");
        Preconditions.checkNotNull(indexes, "The parameter 'indexes' must not be null");
        this.tables = tables;
        this.indexes = indexes;
    }
    
    public final Tables getTables() {
        return tables;
    }
    
    public final Indexes getIndexes() {
        return indexes;
    }
    
    public List<String> getCreateDatabaseQueries() {
        final LinkedList<String> result = Lists.newLinkedList();
        for (final Table t : tables) {
            result.add(t.getCreateTableQuery());
        }
        for (final Index i : indexes) {
            result.add(i.getCreateIndexQuery());
        }
        return result;
    }
    
    public String getCreateDatabaseQuery() {
        final StringBuilder b = new StringBuilder();
        for (final Table t : tables) {
            b.append(t.getCreateTableQuery()).append(";\n");
        }
        for (final Index i : indexes) {
            b.append(i.getCreateIndexQuery()).append(";\n");
        }
        return b.toString();
    }
}