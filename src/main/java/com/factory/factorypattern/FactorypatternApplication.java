package com.factory.factorypattern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FactorypatternApplication {

	public static void main(String[] args) {
		SpringApplication.run(FactorypatternApplication.class, args);
		System.out.println("🏭 Factory Pattern Remittance Service Started!");
		System.out.println("📱 Try: POST http://localhost:8080/api/transfer/send");
	}

}
