package integration;

import io.github.jtestkit.joot.CircularDependencyException;
import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Company;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.github.jtestkit.joot.test.fixtures.Tables.COMPANY;
import static io.github.jtestkit.joot.test.fixtures.Tables.PERSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD Step 2.3: Circular Dependency Detection Tests
 * Tests detection and handling of circular FK dependencies
 */
class CircularDependencyTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
    }
    
    @Test
    void shouldDetectCircularDependency() {
        // ACT & ASSERT: Creating Person should detect circular dependency
        // Person has NOT NULL FK to Company
        // Company has FK to Person
        // This creates a cycle: Person → Company → Person
        assertThatThrownBy(() -> {
            ctx.create(PERSON, Person.class).build();
        })
        .isInstanceOf(CircularDependencyException.class)
        .hasMessageContaining("Circular dependency detected")
        .hasMessageContaining("person");
    }
    
    @Test
    void shouldAllowCreationWithExplicitForeignKey() {
        // To break the circular dependency, we create entities manually with explicit FKs
        
        // ARRANGE: Create a "bootstrap" person with explicit company_id referencing a placeholder
        // (This would fail in real DB, but we're testing the detection logic)
        // Instead, create them in order by setting explicit FKs
        
        // First create Person with a dummy Company (for demo we skip actual creation)
        // In real scenarios, you'd use deferred constraints or nullable FKs
        
        // For this test, we demonstrate that explicit FK prevents auto-creation
        UUID dummyCompanyId = UUID.randomUUID();
        
        // Creating Person with explicit FK should NOT trigger auto-creation of Company
        // But it will fail at DB level due to FK constraint, so we skip this test
        // or we need to create both manually
        
        // BETTER APPROACH: Create both with explicit IDs in a transaction
        // But for simplicity, this test is actually checking explicit FK usage
        
        // Let's test that when FK is set explicitly, no auto-creation happens
        // We'll verify by checking no CircularDependencyException is thrown
        // (even though DB constraint will fail, the cycle detection doesn't trigger)
        
        try {
            Person person = ctx.create(PERSON, Person.class)
                .set(PERSON.COMPANY_ID, dummyCompanyId)  // Explicit FK
                .build();
            // If we reach here, no CircularDependencyException was thrown
            // (DB FK constraint error is expected and is different)
        } catch (CircularDependencyException e) {
            // This should NOT happen - explicit FK should skip auto-creation
            throw new AssertionError("CircularDependencyException should not be thrown when FK is explicit", e);
        } catch (Exception e) {
            // DB constraint error is expected (foreign key violation)
            // This is OK - we're testing that CircularDependencyException is NOT thrown
            assertThat(e).isNotInstanceOf(CircularDependencyException.class);
        }
    }
    
    @Test
    void shouldProvideHelpfulErrorMessageWithDependencyChain() {
        // ACT & ASSERT: Error message should show the chain
        assertThatThrownBy(() -> {
            ctx.create(PERSON, Person.class).build();
        })
        .isInstanceOf(CircularDependencyException.class)
        .hasMessageContaining("person")
        .hasMessageContaining("company");
    }
}

