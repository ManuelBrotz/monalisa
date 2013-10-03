package ch.brotzilla.monalisa.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Schema {
    
    private static final String CreateTable = "CREATE TABLE";
    private static final String CreateIndex = "CREATE INDEX";

    public final Tables tables;
    public final Indexes indexes;
    
    public Schema(Tables tables, Indexes indexes) {
        Preconditions.checkNotNull(tables, "The parameter 'tables' must not be null");
        Preconditions.checkNotNull(indexes, "The parameter 'indexes' must not be null");
        this.tables = tables;
        this.indexes = indexes;
    }
    
    public static class Table {
        
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
            
            this.createTableQuery = CreateTable + " " + name + " (" + fields.fieldDescriptions + ")";
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static final class Tables {
        
        private final ArrayList<Table> list = Lists.newArrayList();
        private final HashMap<String, Table> map = Maps.newHashMap();
        
        public Tables(Table... tables) {
            Preconditions.checkNotNull(tables, "The parameter 'tables' must not be null");
            Preconditions.checkArgument(tables.length > 0, "The parameter 'tables' must not be empty");
            
            for (final Table tbl : tables) {
                Preconditions.checkNotNull(tbl, "The parameter 'tables' must not contain null");
                Preconditions.checkArgument(!map.containsKey(tbl.name), "The parameter 'tables' must not contain duplicate table names (" + tbl.name + ")");
                this.list.add(tbl);
                this.map.put(tbl.name, tbl);
            }
        }
        
        public Table get(int index) {
            return list.get(index);
        }
        
        public Table get(String name) {
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            final Table result = map.get(name);
            if (result == null) 
                throw new IllegalArgumentException("Table '" + name + "' not found");
            return result;
        }
        
        public boolean has(String name) {
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            return map.containsKey(name);
        }
        
        public Table[] getTables() {
            return list.toArray(new Table[list.size()]);
        }
        
        public String[] getTableNames() {
            final String[] names = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                names[i] = list.get(i).name;
            }
            return names;
        }
        
        public int size() {
            return list.size();
        }
        
        public void check(String... tables) {
            Preconditions.checkNotNull(tables, "The parameter 'tables' must not be null");
            Preconditions.checkArgument(tables.length > 0, "The parameter 'tables' must not be empty");

            for (final String tbl : tables) {
                Preconditions.checkNotNull(tbl, "The parameter 'tables' must not contain null");
                if (!map.containsKey(tbl))
                    throw new IllegalArgumentException("Table '" + tbl + "' not found");
            }
        }
    }
    
    public static final class Index {
        
        private final String[] fields;
        
        public final Table table;
        public final String name, createIndexQuery, fieldNames;
        
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
                if (!table.fields.has(f))
                    throw new IllegalArgumentException("Field '" + f + "' not found in table '" + table.name + "'");
                this.fields[i++] = f;
            }
            
            this.fieldNames = Joiner.on(", ").join(fields);   
            this.createIndexQuery = CreateIndex + " " + name + " ON " + table.name + " (" + fieldNames + ")";
        }
        
        public String[] getFields() {
            return Arrays.copyOf(fields, fields.length);
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static final class Indexes {
        
        private final ArrayList<Index> list = Lists.newArrayList();
        private final HashMap<String, Index> map = Maps.newHashMap();
        
        public Indexes(Index... indexes) {
            Preconditions.checkNotNull(indexes, "The parameter 'indexes' must not be null");
            Preconditions.checkArgument(indexes.length > 0, "The parameter 'indexes' must not be empty");
            
            for (final Index idx : indexes) {
                Preconditions.checkNotNull(idx, "The parameter 'indexes' must not contain null");
                Preconditions.checkArgument(!map.containsKey(idx.name), "The parameter 'indexes' must not contain duplicate index names (" + idx.name + ")");
                this.list.add(idx);
                this.map.put(idx.name, idx);
            }
        }
        
        public Index get(int index) {
            return list.get(index);
        }
        
        public Index get(String name) {
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            final Index result = map.get(name);
            if (result == null) 
                throw new IllegalArgumentException("Index '" + name + "' not found");
            return result;
        }
        
        public boolean has(String name) {
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            return map.containsKey(name);
        }
        
        public Index[] getIndexes() {
            return list.toArray(new Index[list.size()]);
        }
        
        public String[] getIndexNames() {
            final String[] names = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                names[i] = list.get(i).name;
            }
            return names;
        }
        
        public int size() {
            return list.size();
        }
        
        public void check(String... indexes) {
            Preconditions.checkNotNull(indexes, "The parameter 'indexes' must not be null");
            Preconditions.checkArgument(indexes.length > 0, "The parameter 'indexes' must not be empty");

            for (final String idx : indexes) {
                Preconditions.checkNotNull(idx, "The parameter 'indexes' must not contain null");
                if (!map.containsKey(idx))
                    throw new IllegalArgumentException("Index '" + idx + "' not found");
            }
        }
    }

    public static final class Field {
        
        public final DataType type;
        public final String name, description;
        public final boolean isNullable, isPrimaryKey;
        
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
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static final class Fields {
        
        private final ArrayList<Field> list = Lists.newArrayList();
        private final HashMap<String, Field> map = Maps.newHashMap();
        
        public final String fieldNames, fieldDescriptions;
        
        public Fields(Field... fields) {
            Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
            Preconditions.checkArgument(fields.length > 0, "The parameter 'fields' must not be empty");
            
            for (final Field f : fields) {
                Preconditions.checkNotNull(f, "The parameter 'fields' must not contain null");
                Preconditions.checkArgument(!map.containsKey(f.name), "The parameter 'fields' must not contain duplicate field names (" + f.name + ")");
                this.list.add(f);
                this.map.put(f.name, f);
            }

            this.fieldNames = Joiner.on(", ").join(getFieldNames());
            this.fieldDescriptions = Joiner.on(", ").join(getFieldDescriptions());
        }
        
        public Field get(int index) {
            return list.get(index);
        }
        
        public Field get(String name) {
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            final Field result = map.get(name);
            if (result == null) 
                throw new IllegalArgumentException("Field '" + name + "' not found");
            return result;
        }
        
        public boolean has(String name) {
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            return map.containsKey(name);
        }
        
        public Field[] getFields() {
            return list.toArray(new Field[list.size()]);
        }
        
        public String[] getFieldNames() {
            final String[] names = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                names[i] = list.get(i).name;
            }
            return names;
        }
        
        public String[] getFieldDescriptions() {
            final String[] descriptions = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                descriptions[i] = list.get(i).description;
            }
            return descriptions;
        }
        
        public int size() {
            return list.size();
        }
        
        public void check(String... fields) {
            Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
            Preconditions.checkArgument(fields.length > 0, "The parameter 'fields' must not be empty");

            for (final String f : fields) {
                Preconditions.checkNotNull(f, "The parameter 'fields' must not contain null");
                if (!map.containsKey(f))
                    throw new IllegalArgumentException("Field '" + f + "' not found");
            }
        }
        
        public static String[] extractFieldNames(Field... fields) {
            Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
            final String[] result = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                result[i] = Preconditions.checkNotNull(fields[i], "The parameter 'fields' must not contain null").name;
            }
            return result;
        }
        
        public static String[] extractFieldDescriptions(Field... fields) {
            Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
            final String[] result = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                result[i] = Preconditions.checkNotNull(fields[i], "The parameter 'fields' must not contain null").description;
            }
            return result;
        }
    }
    
    public static enum DataType {
        
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
}