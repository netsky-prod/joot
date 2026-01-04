package io.github.jtestkit.joot;

import org.jooq.Field;
import org.jooq.Table;

/**
 * Generates values for fields during entity creation.
 * <p>
 * Implementations should be stateless and thread-safe.
 * For stateful generation (e.g., counters), use thread-safe mechanisms like {@link java.util.concurrent.atomic.AtomicLong}.
 * <p>
 * This interface provides two generation methods:
 * <ul>
 *   <li>{@link #generate(int, boolean)} - Simple method with basic constraints (maxLength, isUnique)</li>
 *   <li>{@link #generate(Field, Table)} - Advanced method with full field metadata for semantic generation</li>
 * </ul>
 * <p>
 * Most generators only need to implement the simple method. The advanced method has a default implementation
 * that extracts constraints and delegates to the simple method.
 *
 * @param <T> the type of value to generate
 */
public interface ValueGenerator<T> {
    
    /**
     * Generates a new value with given constraints.
     * <p>
     * This is the primary method that simple generators should implement.
     * 
     * @param maxLength maximum length for String types (0 or negative = unlimited)
     * @param isUnique whether the field has a UNIQUE constraint
     * @return the generated value, never null for non-nullable fields
     */
    T generate(int maxLength, boolean isUnique);
    
    /**
     * Generates a new value based on field metadata.
     * <p>
     * This method provides access to the full field and table metadata,
     * enabling advanced use cases like:
     * <ul>
     *   <li>Semantic generation based on field name (e.g., "email" → "user@example.com")</li>
     *   <li>Enum support (extracting enum values from field type)</li>
     *   <li>Custom constraints from metadata</li>
     * </ul>
     * <p>
     * The default implementation extracts maxLength and isUnique from metadata
     * and delegates to {@link #generate(int, boolean)}.
     * Override this method for semantic or advanced generation logic.
     * 
     * @param field the jOOQ field to generate value for
     * @param table the jOOQ table containing the field (may be null if table context is unavailable)
     * @return the generated value, never null for non-nullable fields
     */
    default T generate(Field<T> field, Table<?> table) {
        int maxLength = field.getDataType().length();
        boolean isUnique = false;
        
        if (table != null) {
            MetadataAnalyzer analyzer = new MetadataAnalyzer();
            isUnique = analyzer.isUniqueField(field, table);
        }
        
        return generate(maxLength, isUnique);
    }
}

