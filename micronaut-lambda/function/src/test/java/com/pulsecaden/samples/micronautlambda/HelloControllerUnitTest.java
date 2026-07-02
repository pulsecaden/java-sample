package com.pulsecaden.samples.micronautlambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HelloControllerUnitTest {

    private final HelloController controller = new HelloController();

    @Test
    void helloBuildsPayloadFromLayerGreeting() {
        assertEquals(Map.of("message", "Hello from Micronaut Lambda layer"), controller.hello());
    }
}
