package br.com.petflow.petflow_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PetflowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetflowApiApplication.class, args);
	}

}
