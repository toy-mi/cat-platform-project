package org.example.catplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
@MapperScan("org.example.catplatform.module.**.mapper")
public class catplatform {
	public static void main(String[] args) {
		SpringApplication.run(catplatform.class, args);
	}
}
