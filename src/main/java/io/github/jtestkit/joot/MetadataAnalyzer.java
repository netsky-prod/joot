package io.github.jtestkit.joot;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.UniqueKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Analyzes jOOQ table metadata to extract foreign key information.
 * Used to understand table dependencies for automatic entity creation.
 * 
 * @since 0.1.0
 */
public class MetadataAnalyzer {
    
    /**
     * Extracts all foreign keys from the given table.
     * 
     * @param table the jOOQ table to analyze
     * @return list of foreign keys, empty if table has no FKs
     */
    public List<ForeignKey<?, ?>> getForeignKeys(Table<?> table) {
        if (table.getReferences() == null) {
            return List.of();
        }
        return new ArrayList<>(table.getReferences());
    }
    
    /**
     * Checks if a field is a foreign key field in the given table.
     * 
     * @param field the field to check
     * @param table the table containing the field
     * @return true if the field is a FK, false otherwise
     */
    public boolean isForeignKeyField(Field<?> field, Table<?> table) {
        return getForeignKeys(table).stream()
            .anyMatch(fk -> fk.getFields().contains(field));
    }
    
    /**
     * Extracts all fields that have UNIQUE constraints from the given table.
     * Includes both single-column UNIQUE constraints and composite UNIQUE keys.
     * 
     * @param table the jOOQ table to analyze
     * @return set of fields with UNIQUE constraints, empty if none
     */
    @SuppressWarnings("rawtypes")
    public Set<Field<?>> getUniqueFields(Table<?> table) {
        Set<Field<?>> uniqueFields = new HashSet<>();
        
        // Get all unique keys (including PRIMARY KEY which is also unique)
        List uniqueKeys = table.getKeys();
        if (uniqueKeys == null || uniqueKeys.isEmpty()) {
            return uniqueFields;
        }
        
        for (Object uk : uniqueKeys) {
            UniqueKey uniqueKey = (UniqueKey) uk;
            // Add all fields from this unique key
            uniqueFields.addAll(uniqueKey.getFields());
        }
        
        // Remove primary key fields (they're unique by definition, but we handle them separately)
        if (table.getPrimaryKey() != null) {
            uniqueFields.removeAll(table.getPrimaryKey().getFields());
        }
        
        return uniqueFields;
    }
    
    /**
     * Checks if a field has a UNIQUE constraint in the given table.
     * 
     * @param field the field to check
     * @param table the table containing the field
     * @return true if the field has UNIQUE constraint, false otherwise
     */
    public boolean isUniqueField(Field<?> field, Table<?> table) {
        return getUniqueFields(table).contains(field);
    }
}

