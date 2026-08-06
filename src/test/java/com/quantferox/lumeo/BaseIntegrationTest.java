package com.quantferox.lumeo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for all integration tests.
 *
 * - Full Spring context with H2 + Liquibase schema
 * - MockMvc without real HTTP server
 * - dev profile - uses H2 in-memory
 * - DataSeeder populates users/categories/products once at context startup
 * - No @Transactional at class level - each test operates on the shared seeded state.
 *   Use @Sql or explicit cleanup in tests that mutate data if isolation is needed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
