package io.github.jtestkit.joot;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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
    private final List<String> activeTraits = new ArrayList<>();
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
    public PojoBuilder<P> trait(String traitName) {
        activeTraits.add(traitName);
        return this;
    }

    @Override
    public List<P> times(int count) {
        List<P> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            results.add(cloneConfiguration().build());
        }
        return results;
    }

    @Override
    public List<P> times(int count, BiConsumer<PojoBuilder<P>, Integer> customizer) {
        List<P> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            PojoBuilderImpl<P> fresh = cloneConfiguration();
            customizer.accept(fresh, i);
            results.add(fresh.build());
        }
        return results;
    }

    private PojoBuilderImpl<P> cloneConfiguration() {
        PojoBuilderImpl<P> clone = new PojoBuilderImpl<>(dsl, table, pojoClass, jootContext, creationChain,
                ((JootContextImpl) jootContext).getGenerateNullablesGlobal());
        if (shouldGenerateNullables != null) {
            clone.shouldGenerateNullables = this.shouldGenerateNullables;
        }
        clone.explicitValues.putAll(this.explicitValues);
        clone.perBuilderGenerators.putAll(this.perBuilderGenerators);
        clone.activeTraits.addAll(this.activeTraits);
        return clone;
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

        // Transfer active traits to RecordBuilder
        for (String traitName : activeTraits) {
            recordBuilder.trait(traitName);
        }
        
        // Build the Record
        Record record = recordBuilder.build();
        
        // Convert Record to POJO
        return record.into(pojoClass);
    }
}
