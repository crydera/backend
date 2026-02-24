package com.crydera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.crydera",
        exclude = UserDetailsServiceAutoConfiguration.class
)
public class CryderaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryderaApplication.class, args);
    }
}
