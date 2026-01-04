package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;

import java.util.UUID;

/**
 * Default generator for UUID values.
 * Generates random UUIDs using {@link UUID#randomUUID()}.
 * UUIDs are inherently unique, so the isUnique parameter is ignored.
 */
public class UuidGenerator implements ValueGenerator<UUID> {
    
    @Override
    public UUID generate(int maxLength, boolean isUnique) {
        // UUID is always unique by nature, ignore isUnique parameter
        return UUID.randomUUID();
    }
}

