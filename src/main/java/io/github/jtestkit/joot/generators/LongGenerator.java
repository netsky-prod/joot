package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default generator for Long values.
 * Generates random longs between 1 and 999 (inclusive).
 * For UNIQUE fields, uses a counter to guarantee uniqueness.
 */
public class LongGenerator implements ValueGenerator<Long> {
    
    private static final AtomicLong UNIQUE_COUNTER = new AtomicLong(1);
    
    @Override
    public Long generate(int maxLength, boolean isUnique) {
        if (isUnique) {
            return UNIQUE_COUNTER.getAndIncrement();
        }
        return ThreadLocalRandom.current().nextLong(1, 1000);
    }
}

