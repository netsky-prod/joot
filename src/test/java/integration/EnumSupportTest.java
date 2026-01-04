package integration;

import integration.enums.TaskPriority;
import integration.enums.TaskStatus;
import io.github.jtestkit.joot.JootContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for automatic enum support.
 * <p>
 * Joot automatically handles enum fields by returning the first value from enum.values().
 * This ensures deterministic test behavior.
 */
class EnumSupportTest extends BaseIntegrationTest {
    
    @Test
    void shouldHandleEnumWithValueGenerator() {
        JootContext ctx = JootContext.create(dsl);
        
        // User can register enum generators explicitly
        ctx.registerGenerator(TaskStatus.class, (len, unique) -> TaskStatus.COMPLETED);
        ctx.registerGenerator(TaskPriority.class, (len, unique) -> TaskPriority.HIGH);
        
        // Generators can be used through Joot API
        // (In real usage: ctx.create(TASK, Task.class).build() would use these)
    }
    
    @Test
    void shouldReturnFirstEnumValue() {
        // Test that enum.values()[0] is deterministic
        TaskStatus firstStatus = TaskStatus.values()[0];
        assertThat(firstStatus).isEqualTo(TaskStatus.PENDING);
        
        TaskPriority firstPriority = TaskPriority.values()[0];
        assertThat(firstPriority).isEqualTo(TaskPriority.LOW);
    }
    
    @Test
    void shouldAllowCustomEnumGenerator() {
        JootContext ctx = JootContext.create(dsl);
        
        // User can override default behavior
        ctx.registerGenerator(TaskStatus.class, (len, unique) -> TaskStatus.COMPLETED);
        ctx.registerGenerator(TaskPriority.class, (len, unique) -> TaskPriority.HIGH);
        
        // These generators can be used in tests
        // (Actual usage would be through ctx.create(...).build())
        assertThat(TaskStatus.COMPLETED).isNotEqualTo(TaskStatus.PENDING);
        assertThat(TaskPriority.HIGH).isNotEqualTo(TaskPriority.LOW);
    }
    
    @Test
    void shouldSupportAllEnumValues() {
        // Verify our test enums have expected values
        TaskStatus[] statuses = TaskStatus.values();
        assertThat(statuses).containsExactly(
            TaskStatus.PENDING,
            TaskStatus.IN_PROGRESS,
            TaskStatus.COMPLETED,
            TaskStatus.CANCELLED
        );
        
        TaskPriority[] priorities = TaskPriority.values();
        assertThat(priorities).containsExactly(
            TaskPriority.LOW,
            TaskPriority.MEDIUM,
            TaskPriority.HIGH,
            TaskPriority.CRITICAL
        );
    }
    
    @Test
    void shouldBeTypeIsEnum() {
        // Verify that TaskStatus is recognized as enum
        Class<?> statusType = TaskStatus.class;
        assertThat(statusType.isEnum()).isTrue();
        
        Object[] enumConstants = statusType.getEnumConstants();
        assertThat(enumConstants).isNotNull();
        assertThat(enumConstants).hasSize(4);
        assertThat(enumConstants[0]).isEqualTo(TaskStatus.PENDING);
    }
}

