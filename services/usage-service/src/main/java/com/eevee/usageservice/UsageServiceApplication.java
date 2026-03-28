package com.eevee.usageservice;

import com.eevee.usageservice.config.UsageGatewayProperties;
import com.eevee.usageservice.config.UsageRabbitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
@EnableConfigurationProperties({UsageRabbitProperties.class, UsageGatewayProperties.class})
public class UsageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsageServiceApplication.class, args);
    }
}
