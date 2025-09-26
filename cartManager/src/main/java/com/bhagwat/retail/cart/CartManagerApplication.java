package com.bhagwat.retail.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.bhagwat.retail")
@EnableJpaRepositories(basePackages = "com.bhagwat.retail.cart.repository")
@ComponentScan(basePackages = {
		"com.bhagwat.retail.cart"
})
public class CartManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartManagerApplication.class, args);
	}

}
