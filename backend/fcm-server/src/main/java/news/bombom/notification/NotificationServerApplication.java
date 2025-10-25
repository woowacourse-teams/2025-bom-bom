package news.bombom.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServerApplication.class, args);
	}

}
