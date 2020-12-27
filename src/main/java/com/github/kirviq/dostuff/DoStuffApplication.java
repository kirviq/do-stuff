package com.github.kirviq.dostuff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DoStuffApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoStuffApplication.class, args);
	}

}
