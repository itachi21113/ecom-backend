package com.example.ecommerce.ecom_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EcomBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomBackendApplication.class, args);
	}

}
