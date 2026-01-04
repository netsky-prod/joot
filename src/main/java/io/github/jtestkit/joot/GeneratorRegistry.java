package io.github.jtestkit.joot;

import io.github.jtestkit.joot.generators.*;
import org.jooq.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for value generators.
 * <p>
 * Manages type-based and field-based generators with the following resolution priority:
 * <ol>
 *   <li>Field-specific generator (highest priority)</li>
 *   <li>Type-based generator</li>
 *   <li>null (if no generator registered)</li>
 * </ol>
 * <p>
 * Built-in generators are pre-registered for common types (String, Integer, Long, UUID, Boolean, LocalDateTime, LocalDate).
 * These can be overridden using {@link #registerTypeGenerator(Class, ValueGenerator)}.
 */
class GeneratorRegistry {
    
    private final Map<Class<?>, ValueGenerator<?>> typeGenerators = new ConcurrentHashMap<>();
    private final Map<Field<?>, ValueGenerator<?>> fieldGenerators = new ConcurrentHashMap<>();
    
    /**
     * Creates a new registry with pre-registered built-in generators.
     */
    public GeneratorRegistry() {
        // Pre-register built-in generators for common types
        // All generators support the advanced generate(Field, Table) method
        // which provides context for adaptive value generation.
        
        typeGenerators.put(String.class, new StringGenerator());
        typeGenerators.put(Integer.class, new IntegerGenerator());
        typeGenerators.put(int.class, new IntegerGenerator());
        typeGenerators.put(Long.class, new LongGenerator());
        typeGenerators.put(long.class, new LongGenerator());
        typeGenerators.put(UUID.class, new UuidGenerator());
        typeGenerators.put(Boolean.class, new BooleanGenerator());
        typeGenerators.put(boolean.class, new BooleanGenerator());
        typeGenerators.put(LocalDateTime.class, new LocalDateTimeGenerator());
        typeGenerators.put(LocalDate.class, new LocalDateGenerator());
    }
    
    /**
     * Registers a generator for a specific field.
     * This takes precedence over type-based generators.
     * 
     * @param field the field to generate values for
     * @param generator the generator to use
     * @param <T> the field type
     */
    public <T> void registerFieldGenerator(Field<T> field, ValueGenerator<T> generator) {
        fieldGenerators.put(field, generator);
    }
    
    /**
     * Registers a generator for a specific type.
     * This overrides built-in generators for the given type.
     * 
     * @param type the type to generate values for
     * @param generator the generator to use
     * @param <T> the value type
     */
    public <T> void registerTypeGenerator(Class<T> type, ValueGenerator<T> generator) {
        typeGenerators.put(type, generator);
    }
    
    /**
     * Resolves a generator for the given field.
     * <p>
     * Resolution priority:
     * 1. Field-specific generator
     * 2. Type-based generator
     * 3. null (no generator found)
     * 
     * @param field the field to resolve generator for
     * @param <T> the field type
     * @return the resolved generator, or null if none found
     */
    @SuppressWarnings("unchecked")
    public <T> ValueGenerator<T> resolve(Field<T> field) {
        // Priority 1: Field-specific generator
        if (fieldGenerators.containsKey(field)) {
            return (ValueGenerator<T>) fieldGenerators.get(field);
        }
        
        // Priority 2: Type-based generator
        Class<?> type = field.getType();
        return (ValueGenerator<T>) typeGenerators.get(type);
    }
    
    /**
     * Checks if a generator is registered for the given field (either field-specific or type-based).
     * 
     * @param field the field to check
     * @return true if a generator is registered, false otherwise
     */
    public boolean hasGenerator(Field<?> field) {
        return fieldGenerators.containsKey(field) || typeGenerators.containsKey(field.getType());
    }
}

