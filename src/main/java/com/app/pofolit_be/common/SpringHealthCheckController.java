package com.app.pofolit_be.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health")
@RestController
public class SpringHealthCheckController {
    @GetMapping
    public String healthCheck() {
        return "good";
    }
}
