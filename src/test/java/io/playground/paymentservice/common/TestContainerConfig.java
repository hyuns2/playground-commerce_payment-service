package io.playground.paymentservice.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
public class TestContainerConfig {
    @Bean
    @ServiceConnection(name = "mysql")
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:9.4.0")
                .withReuse(true);
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>("redis:8.2.1-alpine")
                .withExposedPorts(6379)
                .withReuse(true);
    }
}
