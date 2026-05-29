package io.playground.paymentservice.common;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
//@Testcontainers
//@Import(TestContainerConfig.class)
public abstract class BaseIntegrationTest {
//    @PersistenceContext
//    EntityManager entityManager;
}
