package com.hackforge.orderprocessing;

import com.hackforge.orderprocessing.entity.Product;
import com.hackforge.orderprocessing.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrderProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderProcessingApplication.class, args);
	}

	@Bean
	CommandLineRunner seedProducts(ProductRepository productRepository) {

		return args -> {

			if (productRepository.count() == 0) {

				productRepository.save(
						new Product("iPhone 15", 10)
				);

				productRepository.save(
						new Product("AirPods Pro", 5)
				);

				productRepository.save(
						new Product("MacBook Air", 3)
				);

				System.out.println("=================================");
				System.out.println("DEMO PRODUCTS CREATED");
				System.out.println("iPhone 15      -> 10");
				System.out.println("AirPods Pro    -> 5");
				System.out.println("MacBook Air    -> 3");
				System.out.println("=================================");
			}
		};
	}
}