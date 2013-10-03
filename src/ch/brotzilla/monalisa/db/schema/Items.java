package ch.brotzilla.monalisa.db.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class Items<T extends Item> implements Iterable<T> {
    
    private final ArrayList<T> list = Lists.newArrayList();
    private final HashMap<String, T> map = Maps.newHashMap();
    
    @SuppressWarnings("unchecked")
    protected Items(T... items) {
        Preconditions.checkNotNull(items, "The parameter 'items' must not be null");
        Preconditions.checkArgument(items.length > 0, "The parameter 'items' must not be empty");
        
        for (final T item : items) {
            Preconditions.checkNotNull(item, "The parameter 'items' must not contain null");
            Preconditions.checkArgument(!map.containsKey(item.getName()), "The parameter 'items' must not contain duplicate names (" + item.getName() + ")");
            this.list.add(item);
            this.map.put(item.getName(), item);
        }
    }
    
    public T get(int index) {
        return list.get(index);
    }
    
    public T get(String name) {
        Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
        final T result = map.get(name);
        if (result == null) 
            throw new IllegalArgumentException("Item '" + name + "' not found");
        return result;
    }
    
    public boolean has(String name) {
        Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
        return map.containsKey(name);
    }
    
    @SuppressWarnings("unchecked")
    public T[] get() {
        return (T[]) list.toArray();
    }
    
    public String[] getNames() {
        final String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            names[i] = list.get(i).getName();
        }
        return names;
    }
    
    public int size() {
        return list.size();
    }
    
    public void check(String... names) {
        Preconditions.checkNotNull(names, "The parameter 'names' must not be null");
        Preconditions.checkArgument(names.length > 0, "The parameter 'names' must not be empty");

        for (final String name : names) {
            Preconditions.checkNotNull(name, "The parameter 'names' must not contain null");
            if (!map.containsKey(name))
                throw new IllegalArgumentException("Item '" + name + "' not found");
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            
            private final int length = list.size(); 
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public T next() {
                if (index >= length) 
                    throw new NoSuchElementException();
                return list.get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}