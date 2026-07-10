package com.example.stage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "App is running";
    }

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Public endpoint";
    }

    @GetMapping("/private/hello")
    public String privateHello() {
        return "Private endpoint (LDAP protected)";
    }
}
