package ch.brotzilla.monalisa.db.schema;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class Tables {
    
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