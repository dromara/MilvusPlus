package io.github.javpower.milvus.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class MilvusDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilvusDemoApplication.class, args);
    }

}
