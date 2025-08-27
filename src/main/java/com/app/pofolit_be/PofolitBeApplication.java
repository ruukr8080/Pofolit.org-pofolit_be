package com.app.pofolit_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PofolitBeApplication {

   public static void main(String[] args) {
      SpringApplication.run(PofolitBeApplication.class, args);
   }

}

@RequestMapping("/health")
@RestController
class WebHealthChekController {
   @GetMapping
   public String healthCheck() {
      return "good";
   }
}