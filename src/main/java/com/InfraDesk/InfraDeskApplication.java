package com.InfraDesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class InfraDeskApplication {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl(); // or a mock implementation
    }

    public static void main(String[] args) {
		SpringApplication.run(InfraDeskApplication.class, args);
	}

}


