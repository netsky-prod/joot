package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;
import org.jooq.Field;
import org.jooq.Table;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique email addresses for testing.
 * 
 * <p>Format: {@code test-{counter}@example.com}
 * 
 * <p>Example usage:
 * <pre>
 * ctx.registerGenerator(USER.EMAIL, new EmailGenerator());
 * 
 * User user1 = ctx.create(USER, User.class).build();
 * // user1.email = "test-1@example.com"
 * 
 * User user2 = ctx.create(USER, User.class).build();
 * // user2.email = "test-2@example.com"
 * </pre>
 * 
 * <p>The generator respects the {@code maxLength} parameter and truncates
 * the email if necessary to fit within the field's length constraint.
 */
public class EmailGenerator implements ValueGenerator<String> {
    
    private final AtomicLong counter = new AtomicLong();
    
    @Override
    public String generate(int maxLength, boolean isUnique) {
        long id = counter.incrementAndGet();
        String email = "test-" + id + "@example.com";
        
        // Truncate if exceeds maxLength
        if (maxLength > 0 && email.length() > maxLength) {
            // Try to keep valid email format: "t{id}@ex.com"
            if (maxLength >= 10) {
                email = "t" + id + "@ex.com";
            } else {
                // If still too long, just truncate
                email = email.substring(0, maxLength);
            }
        }
        
        return email;
    }
}

