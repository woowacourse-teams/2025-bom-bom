package me.bombom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class BomBomServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BomBomServerApplication.class, args);
    }

}
