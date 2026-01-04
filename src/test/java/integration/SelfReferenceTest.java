package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.jtestkit.joot.test.fixtures.Tables.CATEGORY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD tests for self-reference (table referencing itself).
 * 
 * Self-reference should:
 * - With generateNullables=false → parent_id = NULL
 * - With generateNullables=true (default) → create parent with depth=1
 */
class SelfReferenceTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldLeaveParentNullInMinimalistMode() {
        // ACT: Create category with generateNullables=false
        Category category = ctx.create(CATEGORY, Category.class)
            .generateNullables(false)
            .build();
        
        // ASSERT: Self-reference is NULL in minimalist mode
        assertThat(category.getParentId()).isNull();
        
        // Only one category in DB
        assertThat(dsl.fetchCount(CATEGORY)).isEqualTo(1);
    }
    
    @Test
    void shouldCreateParentWithDepth1ByDefault() {
        // ACT: Create category with default generateNullables=true
        Category category = ctx.create(CATEGORY, Category.class).build();
        
        // ASSERT: Self-reference creates parent with depth=1
        assertThat(category.getParentId()).isNotNull();
        
        // Parent category exists
        Category parent = dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(category.getParentId()))
            .fetchOneInto(Category.class);
        
        assertThat(parent).isNotNull();
        assertThat(parent.getParentId()).isNull();  // Parent has no parent (depth=1)
        
        // Two categories in DB (child + parent)
        assertThat(dsl.fetchCount(CATEGORY)).isEqualTo(2);
    }
    
    @Test
    void shouldRespectGlobalMinimalistSetting() {
        // ACT: Set global generateNullables=false
        ctx.generateNullables(false);
        
        Category category = ctx.create(CATEGORY, Category.class).build();
        
        // ASSERT: Self-reference is NULL
        assertThat(category.getParentId()).isNull();
        assertThat(dsl.fetchCount(CATEGORY)).isEqualTo(1);
    }
    
    @Test
    void shouldAllowExplicitParent() {
        // ARRANGE: Create parent explicitly
        Category parent = ctx.create(CATEGORY, Category.class)
            .set(CATEGORY.NAME, "Parent Category")
            .generateNullables(false)  // Parent has no parent
            .build();
        
        // ACT: Create child with explicit parent
        Category child = ctx.create(CATEGORY, Category.class)
            .set(CATEGORY.NAME, "Child Category")
            .set(CATEGORY.PARENT_ID, parent.getId())  // Explicit parent
            .build();
        
        // ASSERT: Child references parent
        assertThat(child.getParentId()).isEqualTo(parent.getId());
        assertThat(parent.getParentId()).isNull();
        
        // Two categories in DB
        assertThat(dsl.fetchCount(CATEGORY)).isEqualTo(2);
    }
}

