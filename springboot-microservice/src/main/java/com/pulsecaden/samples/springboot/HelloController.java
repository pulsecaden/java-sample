package com.pulsecaden.samples.springboot;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @GetMapping("/hello")
    Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot microservice");
    }
}
