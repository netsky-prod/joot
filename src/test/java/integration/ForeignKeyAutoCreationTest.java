package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Author;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.AUTHOR;
import static io.github.jtestkit.joot.test.fixtures.Tables.BOOK;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Step 2.2: Foreign Key Auto-Creation Tests
 * Tests automatic creation of parent entities based on FK relationships
 */
class ForeignKeyAutoCreationTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldAutoCreateParentEntityForForeignKey() {
        // ACT: Create a Book (which has FK to Author)
        // Joot should automatically create an Author
        Book book = ctx.create(BOOK, Book.class).build();
        
        // ASSERT: Book is created
        assertThat(book).isNotNull();
        assertThat(book.getId()).isNotNull();
        assertThat(book.getTitle()).isNotNull();
        assertThat(book.getAuthorId()).isNotNull();
        
        // ASSERT: Author was automatically created
        Author author = dsl.selectFrom(AUTHOR)
            .where(AUTHOR.ID.eq(book.getAuthorId()))
            .fetchOneInto(Author.class);
        
        assertThat(author).isNotNull();
        assertThat(author.getId()).isEqualTo(book.getAuthorId());
        assertThat(author.getName()).isNotNull(); // Auto-generated
    }
    
    @Test
    void shouldUseExplicitlySetForeignKeyValue() {
        // ARRANGE: Create Author explicitly
        Author existingAuthor = ctx.create(AUTHOR, Author.class)
            .set(AUTHOR.NAME, "Existing Author")
            .build();
        
        // ACT: Create Book with explicit FK to existing Author
        Book book = ctx.create(BOOK, Book.class)
            .set(BOOK.AUTHOR_ID, existingAuthor.getId())
            .build();
        
        // ASSERT: Book uses existing Author
        assertThat(book.getAuthorId()).isEqualTo(existingAuthor.getId());
        
        // ASSERT: No extra Author was created (still only 1 in DB)
        int authorCount = dsl.fetchCount(AUTHOR);
        assertThat(authorCount).isEqualTo(1);
    }
}

