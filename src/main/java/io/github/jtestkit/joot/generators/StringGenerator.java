package io.github.jtestkit.joot.generators;

import io.github.jtestkit.joot.MetadataAnalyzer;
import io.github.jtestkit.joot.ValueGenerator;
import org.jooq.Field;
import org.jooq.Table;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptive String generator with intelligent length handling and field name awareness.
 * <p>
 * This generator automatically adapts to column length constraints and UNIQUE fields,
 * using the field name as a prefix for better readability in tests:
 * <ul>
 *   <li>VARCHAR(5): "a1", "b2", "c3" (rotating prefix + counter for very short fields)</li>
 *   <li>VARCHAR(10): "n1", "n2" (first char of field name + counter)</li>
 *   <li>VARCHAR(20): "name_1", "title_2" (field name + counter)</li>
 *   <li>VARCHAR(255): "author_name_12345678" (field name + UUID)</li>
 *   <li>TEXT: "bio_1", "description_2" (field name + counter)</li>
 * </ul>
 * <p>
 * Examples:
 * <pre>
 * Author author = ctx.create(AUTHOR, Author.class).build();
 * // author.name = "name_1"
 * // author.bio = "bio_1"
 * 
 * Book book = ctx.create(BOOK, Book.class).build();
 * // book.title = "title_1"
 * // book.isbn = "isbn_1"
 * </pre>
 * <p>
 * This generator is registered by default for String type.
 * 
 * @since 0.1.0
 */
public class StringGenerator implements ValueGenerator<String> {
    
    // Thread-safe counter for generating unique values
    private static final AtomicLong UNIQUE_COUNTER = new AtomicLong(1);
    
    // Prefix characters for very short fields (rotated cyclically)
    private static final char[] PREFIX_CHARS = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w',
        'x', 'y', 'z'
    }; // 23 chars (excluded 'l', 'o', 'u' to avoid confusion with numbers)
    
    @Override
    public String generate(int maxLength, boolean isUnique) {
        // Fallback for direct calls without Field context
        return generateWithFieldName(maxLength, isUnique, "value");
    }
    
    @Override
    public String generate(Field<String> field, Table<?> table) {
        // Extract field name for semantic generation
        String fieldName = field.getName().toLowerCase();
        
        // Get maxLength from field
        int maxLength = field.getDataType().length();
        
        // Check if field is UNIQUE
        MetadataAnalyzer analyzer = new MetadataAnalyzer();
        boolean isUnique = analyzer.isUniqueField(field, table);
        
        // Delegate to method with field name
        return generateWithFieldName(maxLength, isUnique, fieldName);
    }
    
    /**
     * Generates a string value with field name as prefix for better readability.
     * 
     * @param maxLength maximum length constraint (from VARCHAR(N))
     * @param isUnique whether the field has UNIQUE constraint
     * @param fieldPrefix prefix derived from field name (e.g. "name", "email", "title")
     * @return generated string value that fits column length
     */
    private String generateWithFieldName(int maxLength, boolean isUnique, String fieldPrefix) {
        long counter = isUnique ? UNIQUE_COUNTER.getAndIncrement() : 0;
        
        // Unlimited length (TEXT, CLOB, or maxLength <= 0)
        if (maxLength <= 0 || maxLength > 100) {
            if (isUnique) {
                return fieldPrefix + "_" + counter;
            }
            return fieldPrefix + "_" + ThreadLocalRandom.current().nextInt(1, 10000);
        }
        
        // Very short fields (1-5 chars): use rotating prefix + counter
        if (maxLength <= 5) {
            if (isUnique) {
                // Use rotating prefix for variety: "a1", "b2", "c3", ..., "a11", "b12", ...
                char prefix = PREFIX_CHARS[(int)(counter % PREFIX_CHARS.length)];
                String result = String.valueOf(prefix) + counter;
                // Ensure it fits (shouldn't overflow for reasonable counters)
                return result.length() > maxLength ? result.substring(0, maxLength) : result;
            }
            // For non-unique, use rotating prefix + random suffix
            int random = ThreadLocalRandom.current().nextInt(1, 100);
            char prefix = PREFIX_CHARS[random % PREFIX_CHARS.length];
            String result = String.valueOf(prefix) + random;
            return result.length() > maxLength ? result.substring(0, maxLength) : result;
        }
        
        // Short fields (6-10 chars): minimal prefix with counter
        if (maxLength <= 10) {
            String result;
            if (isUnique) {
                // Try to use first char of field name + counter
                char firstChar = fieldPrefix.length() > 0 ? fieldPrefix.charAt(0) : 'f';
                result = firstChar + "" + counter; // "n1", "n2", "n3" for "name"
            } else {
                result = fieldPrefix.substring(0, Math.min(1, fieldPrefix.length())) + 
                         ThreadLocalRandom.current().nextInt(1, 10000);
            }
            return result.length() > maxLength ? result.substring(0, maxLength) : result;
        }
        
        // Medium fields (11-20 chars): short prefix with counter
        if (maxLength <= 20) {
            String result;
            if (isUnique) {
                // Use field name + counter: "name_1", "title_2"
                result = fieldPrefix + "_" + counter;
            } else {
                result = fieldPrefix + "_" + ThreadLocalRandom.current().nextInt(1, 1000);
            }
            return result.length() > maxLength ? result.substring(0, maxLength) : result;
        }
        
        // Longer fields (21-100 chars): full format with field name
        String result;
        if (isUnique) {
            result = fieldPrefix + "_" + counter + "_" + UUID.randomUUID().toString().substring(0, 4);
        } else {
            result = fieldPrefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        }
        return result.length() > maxLength ? result.substring(0, maxLength) : result;
    }
}

