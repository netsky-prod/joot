package io.github.jtestkit.joot;

import org.jooq.Table;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when a circular foreign key dependency is detected.
 * This prevents infinite recursion when auto-creating parent entities.
 * 
 * @since 0.1.0
 */
public class CircularDependencyException extends RuntimeException {
    
    private final List<Table<?>> dependencyChain;
    
    public CircularDependencyException(String message, List<Table<?>> dependencyChain) {
        super(message);
        this.dependencyChain = dependencyChain;
    }
    
    /**
     * Creates an exception with a formatted message showing the dependency chain.
     * 
     * @param dependencyChain the chain of tables that form the circular dependency
     * @return the exception with formatted message
     */
    public static CircularDependencyException fromChain(List<Table<?>> dependencyChain) {
        String chainStr = dependencyChain.stream()
            .map(Table::getName)
            .collect(Collectors.joining(" → "));
        
        String message = String.format(
            "Circular dependency detected in table creation chain: %s → %s",
            chainStr,
            dependencyChain.get(0).getName()
        );
        
        return new CircularDependencyException(message, dependencyChain);
    }
    
    public List<Table<?>> getDependencyChain() {
        return dependencyChain;
    }
}

