package ch.brotzilla.monalisa.db.schema;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public final class Fields extends Items<Field> {
    
    private final String fieldNames, fieldDescriptions;
    
    public Fields(Field... fields) {
        super(fields);
        this.fieldNames = Joiner.on(", ").join(getNames());
        this.fieldDescriptions = Joiner.on(", ").join(getDescriptions());
    }
    
    public String getFieldNames() {
        return fieldNames;
    }
    
    public String getFieldDescriptions() {
        return fieldDescriptions;
    }
    
    public String[] getDescriptions() {
        final String[] descriptions = new String[size()];
        for (int i = 0; i < size(); i++) {
            descriptions[i] = get(i).getDescription();
        }
        return descriptions;
    }
    
    public static String[] extractFieldNames(Field... fields) {
        Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
        final String[] result = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            result[i] = Preconditions.checkNotNull(fields[i], "The parameter 'fields' must not contain null").getName();
        }
        return result;
    }
    
    public static String[] extractFieldDescriptions(Field... fields) {
        Preconditions.checkNotNull(fields, "The parameter 'fields' must not be null");
        final String[] result = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            result[i] = Preconditions.checkNotNull(fields[i], "The parameter 'fields' must not contain null").getDescription();
        }
        return result;
    }
}