package com.pulsecaden.samples.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HelloControllerUnitTest {

    private final HelloController controller = new HelloController();

    @Test
    void helloBuildsExpectedPayload() {
        assertEquals(Map.of("message", "Hello from Spring Boot microservice"), controller.hello());
    }
}
