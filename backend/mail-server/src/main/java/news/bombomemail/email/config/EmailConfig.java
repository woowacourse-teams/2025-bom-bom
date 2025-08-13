package news.bombomemail.email.config;

import jakarta.mail.Session;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Value("${mail.smtp.timeout}")
    private int smtpTimeout;

    @Value("${mail.mime.charset}")
    private String charset;

    @Value("${mail.debug}")
    private boolean debug;

    @Bean
    public Session mailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.timeout", smtpTimeout);
        props.put("mail.mime.charset", charset);
        props.put("mail.debug", debug);
        return Session.getDefaultInstance(props);
    }
}
