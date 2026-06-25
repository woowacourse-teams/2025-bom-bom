package news.bombomemail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BomBomEmailServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BomBomEmailServerApplication.class, args);
    }
}
