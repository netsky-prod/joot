package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;
import org.jooq.Field;
import org.jooq.Table;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates {@link LocalDate} values for testing.
 * 
 * <p>For non-unique fields, returns today's date.
 * For unique fields, generates dates going backwards in time
 * to ensure uniqueness.
 * 
 * <p>Example usage:
 * <pre>
 * ctx.registerGenerator(LocalDate.class, new LocalDateGenerator());
 * 
 * User user1 = ctx.create(USER, User.class).build();
 * // user1.birthDate = 2026-01-02
 * 
 * User user2 = ctx.create(USER, User.class).build();
 * // user2.birthDate = 2026-01-02 (same date - OK for non-unique)
 * </pre>
 * 
 * <p>For unique fields:
 * <pre>
 * Event event1 = ctx.create(EVENT, Event.class).build();
 * // event1.eventDate = 2026-01-02
 * 
 * Event event2 = ctx.create(EVENT, Event.class).build();
 * // event2.eventDate = 2026-01-01 (1 day earlier - unique!)
 * </pre>
 */
public class LocalDateGenerator implements ValueGenerator<LocalDate> {
    
    private final AtomicLong counter = new AtomicLong();
    
    @Override
    public LocalDate generate(int maxLength, boolean isUnique) {
        if (isUnique) {
            // Generate unique dates by going backwards in time
            long daysOffset = counter.incrementAndGet();
            return LocalDate.now().minusDays(daysOffset);
        } else {
            // For non-unique fields, just return today
            return LocalDate.now();
        }
    }
}

