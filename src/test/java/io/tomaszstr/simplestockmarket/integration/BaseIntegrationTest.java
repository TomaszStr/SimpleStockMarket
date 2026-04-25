package io.tomaszstr.simplestockmarket.integration;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import tools.jackson.databind.ObjectMapper;

@SuppressWarnings("unused")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BaseIntegrationTest {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";

    @Autowired
    protected MockMvcTester mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE);
}
