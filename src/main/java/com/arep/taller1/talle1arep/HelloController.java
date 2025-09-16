package com.arep.taller1.talle1arep;

@RestController
public class HelloController {
    @GetMapping("/")
    public String index() {
        return "Hola AREP de Ivan";
    }
}
