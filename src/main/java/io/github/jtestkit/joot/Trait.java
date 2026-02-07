package io.github.jtestkit.joot;

import org.jooq.Field;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A named variation of a factory definition.
 * Traits overlay field defaults and add callbacks on top of a base definition.
 * <p>
 * Immutable once built via {@link TraitBuilder}.
 *
 * @param <R> the Record type
 */
class Trait<R extends Record> {

    private final String name;
    private final Map<Field<?>, Object> overrides;
    private final Map<Field<?>, ValueGenerator<?>> generators;
    private final List<Consumer<Record>> beforeCreateCallbacks;
    private final List<Consumer<Record>> afterCreateCallbacks;

    Trait(String name,
          Map<Field<?>, Object> overrides,
          Map<Field<?>, ValueGenerator<?>> generators,
          List<Consumer<Record>> beforeCreateCallbacks,
          List<Consumer<Record>> afterCreateCallbacks) {
        this.name = name;
        this.overrides = Collections.unmodifiableMap(new LinkedHashMap<>(overrides));
        this.generators = Collections.unmodifiableMap(new LinkedHashMap<>(generators));
        this.beforeCreateCallbacks = Collections.unmodifiableList(new ArrayList<>(beforeCreateCallbacks));
        this.afterCreateCallbacks = Collections.unmodifiableList(new ArrayList<>(afterCreateCallbacks));
    }

    String getName() {
        return name;
    }

    Map<Field<?>, Object> getOverrides() {
        return overrides;
    }

    Map<Field<?>, ValueGenerator<?>> getGenerators() {
        return generators;
    }

    List<Consumer<Record>> getBeforeCreateCallbacks() {
        return beforeCreateCallbacks;
    }

    List<Consumer<Record>> getAfterCreateCallbacks() {
        return afterCreateCallbacks;
    }
}
