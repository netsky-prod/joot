package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.StringLengthTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.STRING_LENGTH_TEST;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests to verify that Joot adapts generated string values
 * to column length constraints.
 * <p>
 * Tests cover:
 * - Very short fields (≤5 chars)
 * - Short fields (≤10 chars)
 * - Medium fields (≤20 chars)
 * - Long fields (≤100 chars)
 * - Unlimited fields (TEXT)
 * <p>
 * For UNIQUE fields, ensures multiple entities can be created without collisions.
 */
class StringLengthAdaptiveTest extends BaseIntegrationTest {

    private JootContext ctx;

    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }

    @Test
    void shouldGenerateStringsThatFitVeryShortFields() {
        // ACT: Create multiple entities with very short fields (3 chars)
        StringLengthTest entity1 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
        StringLengthTest entity2 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
        StringLengthTest entity3 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

        // ASSERT: Generated values fit column constraints
        assertThat(entity1.getTinyField()).isNotNull();
        assertThat(entity1.getTinyField().length()).isLessThanOrEqualTo(3);

        assertThat(entity2.getTinyField()).isNotNull();
        assertThat(entity2.getTinyField().length()).isLessThanOrEqualTo(3);

        assertThat(entity3.getTinyField()).isNotNull();
        assertThat(entity3.getTinyField().length()).isLessThanOrEqualTo(3);
    }

    @Test
    void shouldGenerateUniqueStringsThatFitVeryShortUniqueFields() {
        // ACT: Create multiple entities with very short UNIQUE fields (5 chars)
        StringLengthTest entity1 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
        StringLengthTest entity2 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
        StringLengthTest entity3 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

        // ASSERT: Generated values fit column constraints and are unique
        assertThat(entity1.getTinyUnique()).isNotNull();
        assertThat(entity1.getTinyUnique().length()).isLessThanOrEqualTo(5);

        assertThat(entity2.getTinyUnique()).isNotNull();
        assertThat(entity2.getTinyUnique().length()).isLessThanOrEqualTo(5);

        assertThat(entity3.getTinyUnique()).isNotNull();
        assertThat(entity3.getTinyUnique().length()).isLessThanOrEqualTo(5);

        // All values should be unique
        assertThat(entity1.getTinyUnique()).isNotEqualTo(entity2.getTinyUnique());
        assertThat(entity1.getTinyUnique()).isNotEqualTo(entity3.getTinyUnique());
        assertThat(entity2.getTinyUnique()).isNotEqualTo(entity3.getTinyUnique());

        // All entities should be persisted successfully
        assertThat(dsl.fetchCount(STRING_LENGTH_TEST)).isEqualTo(3);
    }

    @Test
    void shouldGenerateStringsThatFitShortFields() {
        // ACT: Create entity with short fields (8 and 10 chars)
        StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

        // ASSERT: Generated values fit column constraints
        assertThat(entity.getShortField()).isNotNull();
        assertThat(entity.getShortField().length()).isLessThanOrEqualTo(8);

        assertThat(entity.getShortUnique()).isNotNull();
        assertThat(entity.getShortUnique().length()).isLessThanOrEqualTo(10);
    }

    @Test
    void shouldGenerateMultipleUniqueShortStrings() {
        // ACT: Create 10 entities to test short UNIQUE field (10 chars)
        for (int i = 0; i < 10; i++) {
            StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
            assertThat(entity.getShortUnique()).isNotNull();
            assertThat(entity.getShortUnique().length()).isLessThanOrEqualTo(10);
        }

        // ASSERT: All entities created successfully (no unique constraint violations)
        assertThat(dsl.fetchCount(STRING_LENGTH_TEST)).isEqualTo(10);
    }

    @Test
    void shouldGenerateStringsThatFitMediumFields() {
        // ACT: Create entity with medium fields (15 and 20 chars)
        StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

        // ASSERT: Generated values fit column constraints
        assertThat(entity.getMediumField()).isNotNull();
        assertThat(entity.getMediumField().length()).isLessThanOrEqualTo(15);

        assertThat(entity.getMediumUnique()).isNotNull();
        assertThat(entity.getMediumUnique().length()).isLessThanOrEqualTo(20);
    }

    @Test
    void shouldGenerateMultipleUniqueMediumStrings() {
        // ACT: Create 20 entities to test medium UNIQUE field (20 chars)
        for (int i = 0; i < 20; i++) {
            StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
            assertThat(entity.getMediumUnique()).isNotNull();
            assertThat(entity.getMediumUnique().length()).isLessThanOrEqualTo(20);
        }

        // ASSERT: All entities created successfully (no unique constraint violations)
        assertThat(dsl.fetchCount(STRING_LENGTH_TEST)).isEqualTo(20);
    }

    @Test
    void shouldGenerateStringsThatFitLongFields() {
        // ACT: Create entity with long fields (50 and 100 chars)
        StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

        // ASSERT: Generated values fit column constraints
        assertThat(entity.getLongField()).isNotNull();
        assertThat(entity.getLongField().length()).isLessThanOrEqualTo(50);

        assertThat(entity.getLongUnique()).isNotNull();
        assertThat(entity.getLongUnique().length()).isLessThanOrEqualTo(100);
    }

    @Test
    void shouldGenerateMultipleUniqueLongStrings() {
        // ACT: Create 50 entities to test long UNIQUE field (100 chars)
        for (int i = 0; i < 50; i++) {
            StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
            assertThat(entity.getLongUnique()).isNotNull();
            assertThat(entity.getLongUnique().length()).isLessThanOrEqualTo(100);
        }

        // ASSERT: All entities created successfully (no unique constraint violations)
        assertThat(dsl.fetchCount(STRING_LENGTH_TEST)).isEqualTo(50);
    }

    @Test
    void shouldGenerateTextFieldsWithoutConstraints() {
        // ACT: Create entity with unlimited TEXT field
        StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

        // ASSERT: Generated value can be any length
        assertThat(entity.getTextField()).isNotNull();
        // TEXT fields get "generated_" prefix by default, which is longer than short fields
        assertThat(entity.getTextField().length()).isGreaterThan(10);
    }

    @Test
    void shouldAllowExplicitlySetShortValues() {
        // ARRANGE: Set explicit values for very short fields
        String shortValue = "abc";
        String uniqueValue = "x1";

        // ACT: Create entity with explicit short values
        StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class)
            .set(STRING_LENGTH_TEST.TINY_FIELD, shortValue)
            .set(STRING_LENGTH_TEST.TINY_UNIQUE, uniqueValue)
            .build();

        // ASSERT: Explicit values are preserved
        assertThat(entity.getTinyField()).isEqualTo(shortValue);
        assertThat(entity.getTinyUnique()).isEqualTo(uniqueValue);
    }

    @Test
    void shouldHandleMixedExplicitAndGeneratedValues() {
        // ACT: Create entity with some explicit and some generated values
        StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class)
            .set(STRING_LENGTH_TEST.TINY_FIELD, "xyz")
            // tiny_unique, short_field, short_unique, etc. are auto-generated
            .build();

        // ASSERT: Explicit value preserved, others generated within constraints
        assertThat(entity.getTinyField()).isEqualTo("xyz");
        assertThat(entity.getTinyUnique()).isNotNull();
        assertThat(entity.getTinyUnique().length()).isLessThanOrEqualTo(5);
        assertThat(entity.getShortField()).isNotNull();
        assertThat(entity.getShortField().length()).isLessThanOrEqualTo(8);
    }
}

