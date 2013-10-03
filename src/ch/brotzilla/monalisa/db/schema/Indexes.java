package ch.brotzilla.monalisa.db.schema;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class Indexes {
    
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