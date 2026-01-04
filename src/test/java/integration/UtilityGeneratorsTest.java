package integration;

import io.github.jtestkit.joot.JootContext;
import io.github.jtestkit.joot.generators.EmailGenerator;
import io.github.jtestkit.joot.generators.LocalDateGenerator;
import io.github.jtestkit.joot.generators.LocalDateTimeGenerator;
import io.github.jtestkit.joot.generators.PhoneGenerator;
import io.github.jtestkit.joot.test.fixtures.tables.pojos.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.github.jtestkit.joot.test.fixtures.Tables.CONTACT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for utility generators:
 * - EmailGenerator
 * - PhoneGenerator
 * - LocalDateTimeGenerator
 * - LocalDateGenerator
 */
class UtilityGeneratorsTest extends BaseIntegrationTest {
    
    private JootContext ctx;
    
    @BeforeEach
    void setup() {
        ctx = JootContext.create(dsl);
        
        // Register field-specific generators for email and phone
        // Note: LocalDateTime and LocalDate are already registered by default in GeneratorRegistry
        ctx.registerGenerator(CONTACT.EMAIL, new EmailGenerator());
        ctx.registerGenerator(CONTACT.PHONE, new PhoneGenerator());
    }
    
    @Test
    void shouldGenerateUniqueEmails() {
        Contact contact1 = ctx.create(CONTACT, Contact.class).build();
        Contact contact2 = ctx.create(CONTACT, Contact.class).build();
        Contact contact3 = ctx.create(CONTACT, Contact.class).build();
        
        // All emails should be non-null and unique
        assertThat(contact1.getEmail()).isNotNull();
        assertThat(contact2.getEmail()).isNotNull();
        assertThat(contact3.getEmail()).isNotNull();
        
        assertThat(contact1.getEmail()).isNotEqualTo(contact2.getEmail());
        assertThat(contact2.getEmail()).isNotEqualTo(contact3.getEmail());
        assertThat(contact1.getEmail()).isNotEqualTo(contact3.getEmail());
        
        // Should follow format: test-{N}@example.com
        assertThat(contact1.getEmail()).matches("test-\\d+@example\\.com");
        assertThat(contact2.getEmail()).matches("test-\\d+@example\\.com");
    }
    
    @Test
    void shouldGenerateValidPhones() {
        Contact contact1 = ctx.create(CONTACT, Contact.class).build();
        Contact contact2 = ctx.create(CONTACT, Contact.class).build();
        
        // Phones should be non-null and different
        assertThat(contact1.getPhone()).isNotNull();
        assertThat(contact2.getPhone()).isNotNull();
        assertThat(contact1.getPhone()).isNotEqualTo(contact2.getPhone());
        
        // Should follow format: +1-555-XXXX
        assertThat(contact1.getPhone()).matches("\\+1-555-\\d{4}");
        assertThat(contact2.getPhone()).matches("\\+1-555-\\d{4}");
    }
    
    @Test
    void shouldGenerateLocalDateTime() {
        Contact contact1 = ctx.create(CONTACT, Contact.class).build();
        Contact contact2 = ctx.create(CONTACT, Contact.class).build();
        
        // registered_at should be generated
        assertThat(contact1.getRegisteredAt()).isNotNull();
        assertThat(contact2.getRegisteredAt()).isNotNull();
        
        // Should be around current time (within last hour)
        LocalDateTime now = LocalDateTime.now();
        assertThat(contact1.getRegisteredAt()).isAfter(now.minusHours(1));
        assertThat(contact2.getRegisteredAt()).isAfter(now.minusHours(1));
    }
    
    @Test
    void shouldGenerateLocalDate() {
        Contact contact1 = ctx.create(CONTACT, Contact.class).build();
        Contact contact2 = ctx.create(CONTACT, Contact.class).build();
        
        // birth_date should be generated
        assertThat(contact1.getBirthDate()).isNotNull();
        assertThat(contact2.getBirthDate()).isNotNull();
        
        // Should be around today (within last year for backwards dates)
        LocalDate today = LocalDate.now();
        assertThat(contact1.getBirthDate()).isAfter(today.minusYears(1));
        assertThat(contact2.getBirthDate()).isAfter(today.minusYears(1));
    }
    
    @Test
    void shouldRespectFieldLengthForEmail() {
        // Test that EmailGenerator truncates if needed
        // (email field is VARCHAR(255), so this should work fine)
        Contact contact = ctx.create(CONTACT, Contact.class).build();
        
        assertThat(contact.getEmail()).hasSizeLessThanOrEqualTo(255);
    }
    
    @Test
    void shouldRespectFieldLengthForPhone() {
        // Test that PhoneGenerator truncates if needed
        // (phone field is VARCHAR(50), so this should work fine)
        Contact contact = ctx.create(CONTACT, Contact.class).build();
        
        assertThat(contact.getPhone()).hasSizeLessThanOrEqualTo(50);
    }
    
    @Test
    void shouldAllowManualOverride() {
        // User can still override generated values
        Contact contact = ctx.create(CONTACT, Contact.class)
            .set(CONTACT.EMAIL, "custom@test.com")
            .set(CONTACT.PHONE, "+1-999-9999")
            .build();
        
        assertThat(contact.getEmail()).isEqualTo("custom@test.com");
        assertThat(contact.getPhone()).isEqualTo("+1-999-9999");
    }
    
    @Test
    void shouldWorkWithDefaultGenerators() {
        // Create new context WITHOUT field-specific generators (email/phone)
        JootContext freshCtx = JootContext.create(dsl);
        
        // LocalDateTime and LocalDate should work out of the box (registered by default)
        // But email needs manual value or field-specific generator
        Contact contact = freshCtx.create(CONTACT, Contact.class)
            .set(CONTACT.EMAIL, "manual@test.com")  // Required UNIQUE field
            .build();
        
        assertThat(contact.getEmail()).isEqualTo("manual@test.com");
        
        // LocalDateTime and LocalDate are auto-generated (registered by default)
        assertThat(contact.getRegisteredAt()).isNotNull();
        assertThat(contact.getBirthDate()).isNotNull();
        
        // Phone has no field-specific generator, so it's generated as default String with field name
        assertThat(contact.getPhone()).isNotNull().startsWith("phone_");
    }
    
    @Test
    void shouldGenerateMultipleContactsWithAllUtilityGenerators() {
        // Integration test: create multiple contacts using all generators
        Contact contact1 = ctx.create(CONTACT, Contact.class).build();
        Contact contact2 = ctx.create(CONTACT, Contact.class).build();
        Contact contact3 = ctx.create(CONTACT, Contact.class).build();
        
        // All should be created successfully
        assertThat(contact1).isNotNull();
        assertThat(contact2).isNotNull();
        assertThat(contact3).isNotNull();
        
        // All emails should be unique (because of UNIQUE constraint)
        assertThat(contact1.getEmail()).isNotEqualTo(contact2.getEmail());
        assertThat(contact2.getEmail()).isNotEqualTo(contact3.getEmail());
        
        // Verify all fields are populated
        assertThat(contact1.getName()).isNotNull();
        assertThat(contact1.getEmail()).isNotNull();
        assertThat(contact1.getPhone()).isNotNull();
        assertThat(contact1.getRegisteredAt()).isNotNull();
        assertThat(contact1.getBirthDate()).isNotNull();
    }
}

