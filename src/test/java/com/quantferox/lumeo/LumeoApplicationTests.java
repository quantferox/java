package com.quantferox.lumeo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class LumeoApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the full Spring context starts without errors
    }
}
