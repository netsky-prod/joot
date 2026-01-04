package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Book;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static io.github.jtestkit.joot.test.fixtures.Tables.BOOK;
import static io.github.jtestkit.joot.test.fixtures.Tables.PUBLISHER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for per-builder value generators (.withGenerator()).
 * <p>
 * Demonstrates:
 * - Per-builder generators override global generators
 * - Per-builder generators don't affect other entities
 * - Priority: explicit .set() > .withGenerator() > global generators
 */
class WithGeneratorTest extends BaseIntegrationTest {

    private JootContext ctx;

    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }

    @Test
    void shouldUsePerBuilderGenerator() {
        // ACT: Create a book with per-builder generator for title
        Book book = ctx.create(BOOK, Book.class)
            .withGenerator(BOOK.TITLE, (maxLen, isUnique) -> "Per-Builder Title")
            .build();

        // ASSERT: Per-builder generator is used
        assertThat(book.getTitle()).isEqualTo("Per-Builder Title");
    }

    @Test
    void shouldNotAffectOtherEntities() {
        // ACT: Create first book with per-builder generator
        Book book1 = ctx.create(BOOK, Book.class)
            .withGenerator(BOOK.TITLE, (maxLen, isUnique) -> "Custom Title 1")
            .build();

        // Create second book WITHOUT per-builder generator
        Book book2 = ctx.create(BOOK, Book.class).build();

        // ASSERT: First book uses per-builder generator, second uses default
        assertThat(book1.getTitle()).isEqualTo("Custom Title 1");
        assertThat(book2.getTitle()).isNotEqualTo("Custom Title 1");
        assertThat(book2.getTitle()).startsWith("title_"); // Default adaptive logic with field name
    }

    @Test
    void shouldOverrideGlobalGenerator() {
        // ARRANGE: Register global generator
        ctx.registerGenerator(PUBLISHER.NAME, (maxLen, isUnique) -> "Global Publisher");

        // ACT: Create publisher with per-builder generator (overrides global)
        Publisher pub1 = ctx.create(PUBLISHER, Publisher.class)
            .withGenerator(PUBLISHER.NAME, (maxLen, isUnique) -> "Per-Builder Publisher")
            .build();

        // Create another publisher without per-builder (uses global)
        Publisher pub2 = ctx.create(PUBLISHER, Publisher.class).build();

        // ASSERT: Per-builder overrides global
        assertThat(pub1.getName()).isEqualTo("Per-Builder Publisher");
        assertThat(pub2.getName()).isEqualTo("Global Publisher");
    }

    @Test
    void shouldNotOverrideExplicitSetValue() {
        // ACT: Use both .set() and .withGenerator()
        Book book = ctx.create(BOOK, Book.class)
            .set(BOOK.TITLE, "Explicit Title")
            .withGenerator(BOOK.TITLE, (maxLen, isUnique) -> "Generator Title")
            .build();

        // ASSERT: Explicit .set() takes precedence
        assertThat(book.getTitle()).isEqualTo("Explicit Title");
    }

    @Test
    void shouldAllowMultiplePerBuilderGenerators() {
        // ACT: Register multiple per-builder generators
        Book book = ctx.create(BOOK, Book.class)
            .withGenerator(BOOK.TITLE, (maxLen, isUnique) -> "Custom Title")
            .withGenerator(BOOK.ISBN, (maxLen, isUnique) -> "978-CUSTOM-ISBN")
            .build();

        // ASSERT: Both per-builder generators are used
        assertThat(book.getTitle()).isEqualTo("Custom Title");
        assertThat(book.getIsbn()).isEqualTo("978-CUSTOM-ISBN");
    }

    @Test
    void shouldAllowStatefulPerBuilderGenerator() {
        // ARRANGE: Counter-based generator
        AtomicInteger counter = new AtomicInteger(1);

        // ACT: Create multiple publishers with same per-builder generator
        Publisher pub1 = ctx.create(PUBLISHER, Publisher.class)
            .withGenerator(PUBLISHER.NAME, (maxLen, isUnique) -> "Publisher #" + counter.getAndIncrement())
            .build();

        Publisher pub2 = ctx.create(PUBLISHER, Publisher.class)
            .withGenerator(PUBLISHER.NAME, (maxLen, isUnique) -> "Publisher #" + counter.getAndIncrement())
            .build();

        // ASSERT: Counter increments across different builders
        assertThat(pub1.getName()).isEqualTo("Publisher #1");
        assertThat(pub2.getName()).isEqualTo("Publisher #2");
    }

    @Test
    void shouldAccessMaxLengthInPerBuilderGenerator() {
        // ACT: Create book with generator that uses maxLength
        Book book = ctx.create(BOOK, Book.class)
            .withGenerator(BOOK.ISBN, (maxLen, isUnique) -> {
                // ISBN field is VARCHAR(20)
                String value = "A".repeat(30); // Generate longer than maxLen
                return maxLen > 0 ? value.substring(0, maxLen) : value;
            })
            .build();

        // ASSERT: Value is truncated to maxLength
        assertThat(book.getIsbn()).isEqualTo("A".repeat(20)); // ISBN is VARCHAR(20)
        assertThat(book.getIsbn().length()).isLessThanOrEqualTo(20);
    }

    @Test
    void shouldAccessIsUniqueInPerBuilderGenerator() {
        // ACT: Create publisher with generator that checks isUnique
        Publisher publisher = ctx.create(PUBLISHER, Publisher.class)
            .withGenerator(PUBLISHER.NAME, (maxLen, isUnique) -> {
                // PUBLISHER.NAME has UNIQUE constraint
                return isUnique ? "UNIQUE_VALUE" : "REGULAR_VALUE";
            })
            .build();

        // ASSERT: Generator detected UNIQUE constraint
        assertThat(publisher.getName()).isEqualTo("UNIQUE_VALUE");
    }

    @Test
    void shouldWorkForNegativeTests() {
        // Use case: Testing validation with invalid data
        
        // ACT: Create book with intentionally invalid ISBN for testing
        Book invalidBook = ctx.create(BOOK, Book.class)
            .withGenerator(BOOK.ISBN, (maxLen, isUnique) -> "INVALID-ISBN-FORMAT")
            .build();

        // ASSERT: Book created with invalid ISBN (useful for testing validation logic)
        assertThat(invalidBook.getIsbn()).isEqualTo("INVALID-ISBN-FORMAT");
        assertThat(invalidBook.getIsbn()).doesNotStartWith("978-");
    }
}

