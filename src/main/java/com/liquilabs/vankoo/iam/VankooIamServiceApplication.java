package com.liquilabs.vankoo.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableScheduling // Para la limpieza periódica de los tokens de recuperación
@EnableAsync // Para que el envío del correo no corra en el hilo de la petición
public class VankooIamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VankooIamServiceApplication.class, args);
    }

}
