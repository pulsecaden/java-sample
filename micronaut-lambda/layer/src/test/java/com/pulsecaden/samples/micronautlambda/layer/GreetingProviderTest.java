package com.pulsecaden.samples.micronautlambda.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreetingProviderTest {

    private final GreetingProvider greetingProvider = new GreetingProvider();

    @Test
    void messageIdentifiesMicronautLayer() {
        assertEquals("Hello from Micronaut Lambda layer", greetingProvider.message());
    }
}
