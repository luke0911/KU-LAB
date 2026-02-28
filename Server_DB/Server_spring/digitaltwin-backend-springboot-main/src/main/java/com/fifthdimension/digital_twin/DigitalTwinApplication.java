package com.fifthdimension.digital_twin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

@SpringBootApplication(exclude = {ElasticsearchDataAutoConfiguration.class})
public class DigitalTwinApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalTwinApplication.class, args);
	}

}
