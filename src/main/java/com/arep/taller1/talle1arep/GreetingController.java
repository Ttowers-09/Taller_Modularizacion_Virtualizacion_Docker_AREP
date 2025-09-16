package com.arep.taller1.talle1arep;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/hello")
    public String greeting(@RequestParam(value = "name", defaultValue = "Docker!") String name) {
        return String.format(template, name);
    }
}
