package unit;

import io.github.jtestkit.joot.MetadataAnalyzer;
import org.jooq.ForeignKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.jtestkit.joot.test.fixtures.Tables.AUTHOR;
import static io.github.jtestkit.joot.test.fixtures.Tables.BOOK;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Step 2.1: MetadataAnalyzer unit tests
 * Tests extraction of foreign keys from jOOQ metadata
 */
class MetadataAnalyzerTest {
    
    private MetadataAnalyzer analyzer;
    
    @BeforeEach
    void setup() {
        analyzer = new MetadataAnalyzer();
    }
    
    @Test
    void shouldExtractForeignKeys() {
        // ACT: Extract FKs from BOOK table
        List<ForeignKey<?, ?>> fks = analyzer.getForeignKeys(BOOK);
        
        // ASSERT: BOOK has one FK to AUTHOR
        assertThat(fks).hasSize(1);
        
        ForeignKey<?, ?> fk = fks.get(0);
        assertThat(fk.getKey().getTable()).isEqualTo(AUTHOR);
        assertThat(fk.getFields()).hasSize(1);
        assertThat(fk.getFields().get(0).getName()).isEqualTo("author_id");
    }
    
    @Test
    void shouldReturnEmptyListForTableWithoutForeignKeys() {
        // ACT: Extract FKs from AUTHOR table (has no FKs)
        List<ForeignKey<?, ?>> fks = analyzer.getForeignKeys(AUTHOR);
        
        // ASSERT: No foreign keys
        assertThat(fks).isEmpty();
    }
    
    @Test
    void shouldDetectIfFieldIsForeignKey() {
        // ACT & ASSERT: author_id is a FK
        assertThat(analyzer.isForeignKeyField(BOOK.AUTHOR_ID, BOOK)).isTrue();
        
        // ACT & ASSERT: title is NOT a FK
        assertThat(analyzer.isForeignKeyField(BOOK.TITLE, BOOK)).isFalse();
    }
}

