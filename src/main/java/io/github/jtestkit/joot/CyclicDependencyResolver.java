package io.github.jtestkit.joot;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves circular dependencies between tables using two-phase INSERT.
 * 
 * <p>Algorithm:
 * <ol>
 *   <li>Detect cycle in creation chain</li>
 *   <li>Find nullable FK in cycle for temporary break</li>
 *   <li>INSERT first entity with nullable FK = NULL</li>
 *   <li>INSERT second entity with FK to first</li>
 *   <li>UPDATE first entity to set FK to second</li>
 * </ol>
 * 
 * @since 0.1.0
 */
class CyclicDependencyResolver {
    
    private final DSLContext dsl;
    private final MetadataAnalyzer metadataAnalyzer;
    
    CyclicDependencyResolver(DSLContext dsl) {
        this.dsl = dsl;
        this.metadataAnalyzer = new MetadataAnalyzer();
    }
    
    /**
     * Finds nullable FK in the cycle that can be used to break it.
     * 
     * @param parentTable the parent table that creates the cycle
     * @param currentTable the current table being created
     * @param creationChain the chain of tables leading to the cycle
     * @return nullable FK field, or null if cycle is unresolvable
     */
    Field<?> findNullableFKInCycle(Table<?> parentTable, Table<?> currentTable, 
                                    CreationChain creationChain) {
        // Build the full cycle: chain + current + parent
        List<Table<?>> fullCycle = new ArrayList<>(creationChain.getTables());
        fullCycle.add(currentTable);
        fullCycle.add(parentTable);
        
        // Check each table in the cycle for nullable FK to next table in cycle
        for (int i = 0; i < fullCycle.size() - 1; i++) {
            Table<?> from = fullCycle.get(i);
            Table<?> to = fullCycle.get(i + 1);
            
            List<ForeignKey<?, ?>> fks = metadataAnalyzer.getForeignKeys(from);
            for (ForeignKey<?, ?> fk : fks) {
                Field<?> fkField = fk.getFields().get(0);
                Table<?> referencedTable = fk.getKey().getTable();
                
                if (referencedTable.equals(to) && fkField.getDataType().nullable()) {
                    return fkField;  // Found nullable FK in cycle!
                }
            }
        }
        
        return null;  // No nullable FK found - cycle is unresolvable
    }
    
    /**
     * Marks a record as part of cyclic dependency for special cleanup handling.
     * 
     * @param table the table
     * @param pk the primary key field
     * @param pkValue the primary key value
     * @param cyclicFkField the FK field that creates the cycle
     * @return info about cyclic dependency
     */
    CyclicEntityInfo markCyclicEntity(Table<?> table, TableField<?, ?> pk, Object pkValue, 
                                       Field<?> cyclicFkField) {
        return new CyclicEntityInfo(table, pk, pkValue, cyclicFkField);
    }
    
    /**
     * Updates the cyclic FK after parent entity is created.
     * 
     * @param table the table to update
     * @param pk the primary key field
     * @param pkValue the primary key value to identify record
     * @param fkField the FK field to update
     * @param fkValue the FK value to set
     */
    void updateCyclicFK(Table<?> table, TableField<?, ?> pk, Object pkValue, 
                        Field<?> fkField, Object fkValue) {
        dsl.update(table)
            .set((Field<Object>) fkField, fkValue)
            .where(((Field<Object>) pk).eq(pkValue))
            .execute();
    }
    
    /**
     * Breaks cyclic FK before cleanup (sets to NULL).
     * 
     * @param info cyclic entity info
     */
    @SuppressWarnings("unchecked")
    void breakCyclicFKBeforeCleanup(CyclicEntityInfo info) {
        Field<Object> fkField = (Field<Object>) info.cyclicFkField();
        dsl.update(info.table())
            .set(fkField, (Object) null)  // Cast null to Object to avoid ambiguity
            .where(((Field<Object>) info.primaryKey()).eq(info.primaryKeyValue()))
            .execute();
    }
    
    /**
     * Information about entity that is part of cyclic dependency.
     */
    record CyclicEntityInfo(
        Table<?> table,
        TableField<?, ?> primaryKey,
        Object primaryKeyValue,
        Field<?> cyclicFkField
    ) {}
}

