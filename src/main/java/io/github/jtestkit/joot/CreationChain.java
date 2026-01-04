package io.github.jtestkit.joot;

import org.jooq.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tracks the chain of table creations to detect circular dependencies.
 * Immutable value object - each operation returns a new instance.
 * 
 * @since 0.1.0
 */
class CreationChain {
    
    private final List<Table<?>> tables;
    
    /**
     * Creates an empty creation chain.
     */
    static CreationChain empty() {
        return new CreationChain(Collections.emptyList());
    }
    
    private CreationChain(List<Table<?>> tables) {
        this.tables = Collections.unmodifiableList(new ArrayList<>(tables));
    }
    
    /**
     * Returns a new chain with the given table added.
     * 
     * @param table the table to add
     * @return a new CreationChain with the table added
     */
    CreationChain add(Table<?> table) {
        List<Table<?>> newList = new ArrayList<>(tables);
        newList.add(table);
        return new CreationChain(newList);
    }
    
    /**
     * Checks if the chain contains the given table.
     * Compares by table name to handle jOOQ table instance differences.
     * 
     * @param table the table to check
     * @return true if the table is in the chain, false otherwise
     */
    boolean contains(Table<?> table) {
        String tableName = table.getName();
        return tables.stream()
            .anyMatch(t -> t.getName().equals(tableName));
    }
    
    /**
     * Returns a copy of the tables in the chain.
     * Used for error reporting.
     * 
     * @return an unmodifiable list of tables
     */
    List<Table<?>> getTables() {
        return tables;
    }
    
    /**
     * Creates a chain showing the circular dependency path.
     * 
     * @param currentTable the table currently being created
     * @param parentTable the parent table that creates the cycle
     * @return a list representing the full cycle
     */
    List<Table<?>> buildCycleChain(Table<?> currentTable, Table<?> parentTable) {
        List<Table<?>> cycle = new ArrayList<>(tables);
        cycle.add(currentTable);
        cycle.add(parentTable);
        return cycle;
    }
    
    @Override
    public String toString() {
        return tables.stream()
            .map(Table::getName)
            .collect(Collectors.joining(" → "));
    }
}

