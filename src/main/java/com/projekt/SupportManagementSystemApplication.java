package com.projekt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SupportManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportManagementSystemApplication.class, args);
    }

}
