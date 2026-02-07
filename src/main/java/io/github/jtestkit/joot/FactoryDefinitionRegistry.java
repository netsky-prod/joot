package io.github.jtestkit.joot;

import org.jooq.Record;
import org.jooq.Table;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for factory definitions, keyed by table name.
 * Thread-safe via ConcurrentHashMap (same pattern as {@link GeneratorRegistry}).
 */
class FactoryDefinitionRegistry {

    private final ConcurrentHashMap<String, FactoryDefinition<?>> definitions = new ConcurrentHashMap<>();

    /**
     * Registers a definition for a table (keyed by table name).
     */
    <R extends Record> void register(Table<R> table, FactoryDefinition<R> definition) {
        definitions.put(table.getName().toLowerCase(), definition);
    }

    /**
     * Resolves a definition for a table. Returns null if none registered.
     */
    @SuppressWarnings("unchecked")
    <R extends Record> FactoryDefinition<R> resolve(Table<R> table) {
        return (FactoryDefinition<R>) definitions.get(table.getName().toLowerCase());
    }
}
