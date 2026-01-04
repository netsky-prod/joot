package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Author;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Book;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.AUTHOR;
import static io.github.jtestkit.joot.test.fixtures.Tables.BOOK;
import static io.github.jtestkit.joot.test.fixtures.Tables.PUBLISHER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Step 4: UNIQUE Constraints Tests
 * Tests that UNIQUE fields generate unique values automatically.
 */
class UniqueConstraintTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldGenerateUniqueValuesForUniqueField() {
        // ACT: Create multiple authors (email is UNIQUE)
        Author author1 = ctx.create(AUTHOR, Author.class).build();
        Author author2 = ctx.create(AUTHOR, Author.class).build();
        Author author3 = ctx.create(AUTHOR, Author.class).build();
        
        // ASSERT: All emails are different (no DB constraint violation)
        assertThat(author1.getEmail()).isNotNull();
        assertThat(author2.getEmail()).isNotNull();
        assertThat(author3.getEmail()).isNotNull();
        
        assertThat(author1.getEmail()).isNotEqualTo(author2.getEmail());
        assertThat(author1.getEmail()).isNotEqualTo(author3.getEmail());
        assertThat(author2.getEmail()).isNotEqualTo(author3.getEmail());
        
        // All authors created successfully (no constraint violation)
        assertThat(dsl.fetchCount(AUTHOR)).isEqualTo(3);
    }
    
    @Test
    void shouldGenerateUniqueISBNForBooks() {
        // ACT: Create multiple books (isbn is UNIQUE)
        Book book1 = ctx.create(BOOK, Book.class).build();
        Book book2 = ctx.create(BOOK, Book.class).build();
        Book book3 = ctx.create(BOOK, Book.class).build();
        
        // ASSERT: All ISBNs are different
        assertThat(book1.getIsbn()).isNotNull();
        assertThat(book2.getIsbn()).isNotNull();
        assertThat(book3.getIsbn()).isNotNull();
        
        assertThat(book1.getIsbn()).isNotEqualTo(book2.getIsbn());
        assertThat(book1.getIsbn()).isNotEqualTo(book3.getIsbn());
        assertThat(book2.getIsbn()).isNotEqualTo(book3.getIsbn());
        
        // All books created successfully
        assertThat(dsl.fetchCount(BOOK)).isEqualTo(3);
    }
    
    @Test
    void shouldGenerateUniquePublisherNames() {
        // ACT: Create multiple publishers (name is UNIQUE)
        Publisher pub1 = ctx.create(PUBLISHER, Publisher.class).build();
        Publisher pub2 = ctx.create(PUBLISHER, Publisher.class).build();
        Publisher pub3 = ctx.create(PUBLISHER, Publisher.class).build();
        
        // ASSERT: All names are different
        assertThat(pub1.getName()).isNotNull();
        assertThat(pub2.getName()).isNotNull();
        assertThat(pub3.getName()).isNotNull();
        
        assertThat(pub1.getName()).isNotEqualTo(pub2.getName());
        assertThat(pub1.getName()).isNotEqualTo(pub3.getName());
        assertThat(pub2.getName()).isNotEqualTo(pub3.getName());
        
        // All publishers created successfully
        assertThat(dsl.fetchCount(PUBLISHER)).isEqualTo(3);
    }
    
    @Test
    void shouldGenerateUniqueValuesForFKAutoCreation() {
        // ACT: Create multiple books (each auto-creates author with unique email)
        Book book1 = ctx.create(BOOK, Book.class).build();
        Book book2 = ctx.create(BOOK, Book.class).build();
        Book book3 = ctx.create(BOOK, Book.class).build();
        
        // ASSERT: 3 different authors created (no email collision)
        assertThat(dsl.fetchCount(AUTHOR)).isEqualTo(3);
        
        // Each book has different author
        assertThat(book1.getAuthorId()).isNotEqualTo(book2.getAuthorId());
        assertThat(book1.getAuthorId()).isNotEqualTo(book3.getAuthorId());
        assertThat(book2.getAuthorId()).isNotEqualTo(book3.getAuthorId());
        
        // Verify authors have different emails
        Author author1 = ctx.get(book1.getAuthorId(), AUTHOR, Author.class);
        Author author2 = ctx.get(book2.getAuthorId(), AUTHOR, Author.class);
        Author author3 = ctx.get(book3.getAuthorId(), AUTHOR, Author.class);
        
        assertThat(author1.getEmail()).isNotEqualTo(author2.getEmail());
        assertThat(author1.getEmail()).isNotEqualTo(author3.getEmail());
        assertThat(author2.getEmail()).isNotEqualTo(author3.getEmail());
    }
    
    @Test
    void shouldAllowExplicitValueForUniqueField() {
        // ACT: Set explicit value for UNIQUE field
        Author author = ctx.create(AUTHOR, Author.class)
            .set(AUTHOR.EMAIL, "explicit@example.com")
            .build();
        
        // ASSERT: Explicit value is used
        assertThat(author.getEmail()).isEqualTo("explicit@example.com");
    }
}

