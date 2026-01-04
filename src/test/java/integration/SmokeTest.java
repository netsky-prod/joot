package integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmokeTest extends BaseIntegrationTest {
    
    @Test
    void shouldConnectToDatabase() {
        int result = dsl.selectOne().fetchOne(0, int.class);
        assertThat(result).isEqualTo(1);
    }
}

