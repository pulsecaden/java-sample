package com.pulsecaden.samples.corelambda.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreetingProviderTest {

    private final GreetingProvider greetingProvider = new GreetingProvider();

    @Test
    void messageIdentifiesCoreJavaLayer() {
        assertEquals("Hello from Core Java Lambda layer", greetingProvider.message());
    }
}
