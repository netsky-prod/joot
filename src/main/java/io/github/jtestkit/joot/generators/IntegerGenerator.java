package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default generator for Integer values.
 * Generates random integers between 1 and 999 (inclusive).
 * For UNIQUE fields, uses a counter to guarantee uniqueness.
 */
public class IntegerGenerator implements ValueGenerator<Integer> {
    
    private static final AtomicLong UNIQUE_COUNTER = new AtomicLong(1);
    
    @Override
    public Integer generate(int maxLength, boolean isUnique) {
        if (isUnique) {
            return (int) UNIQUE_COUNTER.getAndIncrement();
        }
        return ThreadLocalRandom.current().nextInt(1, 1000);
    }
}

