package com.Proyecto.stoq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableMethodSecurity
public class StoqApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoqApplication.class, args);
    }

}