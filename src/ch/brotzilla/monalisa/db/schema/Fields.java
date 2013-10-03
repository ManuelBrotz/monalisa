package ch.brotzilla.monalisa.db.schema;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class Fields {
    
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