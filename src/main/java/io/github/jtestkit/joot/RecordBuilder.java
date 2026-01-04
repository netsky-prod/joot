package io.github.jtestkit.joot;

import org.jooq.Field;
import org.jooq.Record;

/**
 * Fluent builder for creating jOOQ Record entities.
 * Alternative to PojoBuilder for cases when working directly with jOOQ Records is preferred.
 * 
 * <p>Example usage:
 * <pre>{@code
 * AuthorRecord author = ctx.createRecord(AUTHOR)
 *     .set(AUTHOR.NAME, "Isaac Asimov")
 *     .build();
 * }</pre>
 * 
 * @param <R> the Record type
 * @since 0.1.0
 */
public interface RecordBuilder<R extends Record> {
    
    /**
     * Sets an explicit value for a field.
     * 
     * @param field the field to set
     * @param value the value to set
     * @param <T> the field type
     * @return this builder for chaining
     */
    <T> RecordBuilder<R> set(Field<T> field, T value);
    
    /**
     * Controls generation of nullable fields for this specific build.
     * Overrides the global context setting.
     * By default (true), nullable fields are generated for production-like objects.
     * Set to false for minimalist mode (only NOT NULL fields).
     * 
     * @param generate true to generate nullable fields, false to skip them
     * @return this builder for chaining
     */
    RecordBuilder<R> generateNullables(boolean generate);
    
    /**
     * Registers a custom value generator for a specific field in this builder only.
     * This takes precedence over global field-specific generators and type-based generators,
     * but not over explicit values set via {@link #set(Field, Object)}.
     * <p>
     * Use this for per-entity custom generation logic (e.g., negative tests, edge cases).
     * 
     * @param field the field to generate values for
     * @param generator the generator to use
     * @param <T> the field type
     * @return this builder for chaining
     */
    <T> RecordBuilder<R> withGenerator(Field<T> field, ValueGenerator<T> generator);
    
    /**
     * Creates the entity in the database and returns the Record.
     * 
     * @return the created Record with all fields populated (including generated PKs)
     */
    R build();
}

