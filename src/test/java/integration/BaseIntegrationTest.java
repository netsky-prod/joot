package integration;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Testcontainers
public abstract class BaseIntegrationTest {
    
    @Container
    protected static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");
    
    protected DSLContext dsl;
    
    @BeforeEach
    void setupDSL() throws Exception {
        // Create DSLContext with PostgreSQL connection
        Connection connection = DriverManager.getConnection(
            postgres.getJdbcUrl(),
            postgres.getUsername(),
            postgres.getPassword()
        );
        
        dsl = DSL.using(connection, SQLDialect.POSTGRES);
        
        // Load schema with DROP IF EXISTS to ensure clean state
        loadSchema();
    }
    
    @AfterEach
    void cleanupAfterTest() {
        // Clean up data after each test
        if (dsl != null) {
            try {
                // Drop in reverse order of dependencies
                dsl.execute("DROP TABLE IF EXISTS task CASCADE");
                dsl.execute("DROP TABLE IF EXISTS message CASCADE");
                dsl.execute("DROP TABLE IF EXISTS team CASCADE");
                dsl.execute("DROP TABLE IF EXISTS users CASCADE");
                dsl.execute("DROP TABLE IF EXISTS category CASCADE");
                dsl.execute("DROP TABLE IF EXISTS orders CASCADE");
                dsl.execute("DROP TABLE IF EXISTS product CASCADE");
                dsl.execute("DROP TABLE IF EXISTS publisher CASCADE");
                dsl.execute("DROP TABLE IF EXISTS string_length_test CASCADE");
                dsl.execute("DROP TABLE IF EXISTS book CASCADE");
                dsl.execute("DROP TABLE IF EXISTS author CASCADE");
                dsl.execute("DROP TABLE IF EXISTS contact CASCADE");
                dsl.execute("DROP TABLE IF EXISTS article CASCADE");
                dsl.execute("DROP TABLE IF EXISTS person CASCADE");
                dsl.execute("DROP TABLE IF EXISTS company CASCADE");
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
    
    private void loadSchema() throws Exception {
        String schema = Files.readString(
            Path.of("src/test/resources/test-schema.sql")
        );
        
        // Execute schema SQL
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute(schema);
        }
    }
}

