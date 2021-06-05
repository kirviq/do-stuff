package com.github.kirviq.dostuff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication(proxyBeanMethods = false)
public class DoStuffApplication implements WebMvcConfigurer {
	
	private final BasicAuthInterceptor basicAuthInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(basicAuthInterceptor);
	}
	
	public static void main(String[] args) {
		log.info("starting server");
		SpringApplication.run(DoStuffApplication.class, args);
	}
	
}
