package com.pulsecaden.samples.micronautlambda;

import com.pulsecaden.samples.micronautlambda.layer.GreetingProvider;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import java.util.Map;

@Controller("/hello")
class HelloController {

    private final GreetingProvider greetingProvider = new GreetingProvider();

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, String> hello() {
        return Map.of("message", greetingProvider.message());
    }
}
