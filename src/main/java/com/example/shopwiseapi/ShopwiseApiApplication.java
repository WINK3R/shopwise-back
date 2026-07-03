package com.example.shopwiseapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ShopwiseApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopwiseApiApplication.class, args);
    }

}
