package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Default generator for Boolean values.
 * Generates random boolean values with 50/50 probability.
 * The maxLength and isUnique parameters are ignored for boolean types.
 */
public class BooleanGenerator implements ValueGenerator<Boolean> {
    
    @Override
    public Boolean generate(int maxLength, boolean isUnique) {
        // Boolean doesn't need length or uniqueness constraints
        return ThreadLocalRandom.current().nextBoolean();
    }
}

