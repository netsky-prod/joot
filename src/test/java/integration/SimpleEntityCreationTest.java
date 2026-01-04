package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.AUTHOR;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Step 1.2: Simple entity creation without FK
 * Tests creating Author entity (no foreign keys)
 */
class SimpleEntityCreationTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setupContext() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldCreateAuthor() {
        // ACT: Create author using PojoBuilder
        Author author = ctx.create(AUTHOR, Author.class).build();
        
        // ASSERT: Author was created
        assertThat(author).isNotNull();
        assertThat(author.getId()).isNotNull();
        assertThat(author.getName()).isNotNull();  // generated value
        
        // ASSERT: Author exists in database
        Author fromDb = dsl.selectFrom(AUTHOR)
            .where(AUTHOR.ID.eq(author.getId()))
            .fetchOneInto(Author.class);
        
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getName()).isEqualTo(author.getName());
    }
}

