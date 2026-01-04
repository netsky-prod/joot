package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.ValueGenerator;
import org.jooq.Field;
import org.jooq.Table;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique phone numbers for testing.
 * 
 * <p>Format: {@code +1-555-{counter}} (US test phone numbers)
 * 
 * <p>Example usage:
 * <pre>
 * ctx.registerGenerator(USER.PHONE, new PhoneGenerator());
 * 
 * User user1 = ctx.create(USER, User.class).build();
 * // user1.phone = "+1-555-0001"
 * 
 * User user2 = ctx.create(USER, User.class).build();
 * // user2.phone = "+1-555-0002"
 * </pre>
 * 
 * <p>Uses the reserved US phone number range 555-0100 to 555-0199
 * which is designated for fictional use.
 * 
 * <p>The generator respects the {@code maxLength} parameter and adjusts
 * the format if necessary to fit within the field's length constraint.
 */
public class PhoneGenerator implements ValueGenerator<String> {
    
    private final AtomicLong counter = new AtomicLong(100); // Start from 555-0100
    
    @Override
    public String generate(int maxLength, boolean isUnique) {
        long id = counter.incrementAndGet();
        String phone = String.format("+1-555-%04d", id % 10000);
        
        // Adjust format if maxLength is constrained
        if (maxLength > 0 && phone.length() > maxLength) {
            if (maxLength >= 10) {
                // Shorter format: 555-{id}
                phone = String.format("555-%04d", id % 10000);
            } else if (maxLength >= 7) {
                // Even shorter: 555{id}
                phone = String.format("555%04d", id % 10000);
            } else {
                // Just use numbers
                phone = String.format("%d", id);
            }
            
            // Final truncation if still too long
            if (phone.length() > maxLength) {
                phone = phone.substring(0, maxLength);
            }
        }
        
        return phone;
    }
}

