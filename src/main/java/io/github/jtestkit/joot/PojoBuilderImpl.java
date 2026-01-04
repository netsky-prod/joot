package io.github.jtestkit.joot;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of PojoBuilder.
 * Delegates entity creation to RecordBuilder and converts the result to POJO.
 * 
 * @param <P> the POJO type
 * @since 0.1.0
 */
class PojoBuilderImpl<P> implements PojoBuilder<P> {
    
    private final DSLContext dsl;
    private final Table<?> table;
    private final Class<P> pojoClass;
    private final JootContext jootContext;
    private final CreationChain creationChain;
    private final Map<Field<?>, Object> explicitValues = new HashMap<>();
    private final Map<Field<?>, ValueGenerator<?>> perBuilderGenerators = new HashMap<>();
    private Boolean shouldGenerateNullables = null;  // null = use context default
    
    PojoBuilderImpl(DSLContext dsl, Table<?> table, Class<P> pojoClass, 
                    JootContext jootContext,
                    CreationChain creationChain,
                    boolean generateNullables) {
        this.dsl = dsl;
        this.table = table;
        this.pojoClass = pojoClass;
        this.jootContext = jootContext;
        this.creationChain = creationChain;
        // Start with initial value, can be overridden by builder
        if (generateNullables) {
            this.shouldGenerateNullables = true;
        }
    }
    
    @Override
    public <T> PojoBuilder<P> set(Field<T> field, T value) {
        explicitValues.put(field, value);
        return this;
    }
    
    @Override
    public PojoBuilder<P> generateNullables(boolean generate) {
        this.shouldGenerateNullables = generate;
        return this;
    }
    
    @Override
    public <T> PojoBuilder<P> withGenerator(Field<T> field, ValueGenerator<T> generator) {
        perBuilderGenerators.put(field, generator);
        return this;
    }
    
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public P build() {
        // Delegate to RecordBuilder for all entity creation logic
        // (FK auto-creation, value generation, insertion)
        RecordBuilder recordBuilder = new RecordBuilderImpl<>(
            dsl,
            (Table) table,
            jootContext,
            creationChain,
            ((JootContextImpl) jootContext).getGenerateNullablesGlobal()
        );
        
        // Transfer generateNullables flag if explicitly set
        if (shouldGenerateNullables != null) {
            recordBuilder.generateNullables(shouldGenerateNullables);
        }
        
        // Transfer all explicit values to RecordBuilder
        for (Map.Entry<Field<?>, Object> entry : explicitValues.entrySet()) {
            recordBuilder.set(entry.getKey(), entry.getValue());
        }
        
        // Transfer all per-builder generators to RecordBuilder
        for (Map.Entry<Field<?>, ValueGenerator<?>> entry : perBuilderGenerators.entrySet()) {
            recordBuilder.withGenerator((Field) entry.getKey(), entry.getValue());
        }
        
        // Build the Record
        Record record = recordBuilder.build();
        
        // Convert Record to POJO
        return record.into(pojoClass);
    }
}
