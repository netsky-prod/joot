package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Author;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.github.jtestkit.joot.test.fixtures.Tables.AUTHOR;
import static io.github.jtestkit.joot.test.fixtures.Tables.BOOK;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Step 6.1: Data Access API Tests
 * Tests for convenient access to created entities via ctx.get().
 */
class DataAccessTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldGetEntityByPrimaryKey() {
        // ARRANGE: Create book (auto-creates author)
        Book book = ctx.create(BOOK, Book.class).build();
        
        // ACT: Get author by PK
        Author author = ctx.get(book.getAuthorId(), AUTHOR, Author.class);
        
        // ASSERT: Author retrieved successfully
        assertThat(author).isNotNull();
        assertThat(author.getId()).isEqualTo(book.getAuthorId());
        assertThat(author.getName()).isNotNull();
    }
    
    @Test
    void shouldReturnNullWhenEntityNotFound() {
        // ACT: Try to get non-existent entity
        UUID nonExistentId = UUID.randomUUID();
        Author author = ctx.get(nonExistentId, AUTHOR, Author.class);
        
        // ASSERT: Returns null (not exception)
        assertThat(author).isNull();
    }
    
    @Test
    void shouldGetWorkWithExplicitlySetPrimaryKey() {
        // ARRANGE: Create author with explicit ID
        UUID explicitId = UUID.randomUUID();
        Author createdAuthor = ctx.create(AUTHOR, Author.class)
            .set(AUTHOR.ID, explicitId)
            .set(AUTHOR.NAME, "Explicit Author")
            .build();
        
        // ACT: Get by explicit PK
        Author retrievedAuthor = ctx.get(explicitId, AUTHOR, Author.class);
        
        // ASSERT: Retrieved successfully
        assertThat(retrievedAuthor).isNotNull();
        assertThat(retrievedAuthor.getId()).isEqualTo(explicitId);
        assertThat(retrievedAuthor.getName()).isEqualTo("Explicit Author");
    }
}

