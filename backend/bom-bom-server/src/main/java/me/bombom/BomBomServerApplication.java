package me.bombom;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class BomBomServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BomBomServerApplication.class, args);
    }

}
