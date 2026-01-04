package integration;

import io.github.jtestkit.joot.JootContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Step 1.1: JootContext creation test
 * RED phase - this test will not compile yet
 */
class JootContextCreationTest extends BaseIntegrationTest {
    
    @Test
    void shouldCreateJootContext() {
        // ACT: Create JootContext
        JootContext ctx = JootContext.create(dsl);
        
        // ASSERT: Context is created and DSLContext is accessible
        assertThat(ctx).isNotNull();
        assertThat(ctx.dsl()).isSameAs(dsl);
    }
}

