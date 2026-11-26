package org.paytrack.com;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Disabled("Requires local Docker daemon for Testcontainers integration testing")
class PaytrackApplicationTests {

    @Test
    void contextLoads() {
    }

}
