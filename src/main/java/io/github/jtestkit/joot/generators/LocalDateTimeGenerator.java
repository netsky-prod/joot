package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;
import org.jooq.Field;
import org.jooq.Table;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates {@link LocalDateTime} values for testing.
 * 
 * <p>For non-unique fields, returns the current timestamp.
 * For unique fields, generates timestamps going backwards in time
 * to ensure uniqueness.
 * 
 * <p>Example usage:
 * <pre>
 * ctx.registerGenerator(LocalDateTime.class, new LocalDateTimeGenerator());
 * 
 * Article article1 = ctx.create(ARTICLE, Article.class).build();
 * // article1.createdAt = 2026-01-02 19:00:00
 * 
 * Article article2 = ctx.create(ARTICLE, Article.class).build();
 * // article2.createdAt = 2026-01-02 19:00:00 (same time - OK for non-unique)
 * </pre>
 * 
 * <p>For unique fields:
 * <pre>
 * Event event1 = ctx.create(EVENT, Event.class).build();
 * // event1.eventTime = 2026-01-02 19:00:00
 * 
 * Event event2 = ctx.create(EVENT, Event.class).build();
 * // event2.eventTime = 2026-01-02 18:59:59 (1 second earlier - unique!)
 * </pre>
 */
public class LocalDateTimeGenerator implements ValueGenerator<LocalDateTime> {
    
    private final AtomicLong counter = new AtomicLong();
    
    @Override
    public LocalDateTime generate(int maxLength, boolean isUnique) {
        if (isUnique) {
            // Generate unique timestamps by going backwards in time
            long secondsOffset = counter.incrementAndGet();
            return LocalDateTime.now().minusSeconds(secondsOffset);
        } else {
            // For non-unique fields, just return current time
            return LocalDateTime.now();
        }
    }
}

