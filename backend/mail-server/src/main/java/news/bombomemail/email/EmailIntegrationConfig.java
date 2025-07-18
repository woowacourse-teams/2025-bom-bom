package news.bombomemail.email;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;


@Profile("!test")
@Configuration
@EnableIntegration
public class EmailIntegrationConfig {

    @Value("${integration.file.maildir}")
    private String mailDir;

    @Bean
    public MessageSource<File> mailFileSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(mailDir));

        CompositeFileListFilter<File> filter = new CompositeFileListFilter<>();
        filter.addFilter(new SimplePatternFileListFilter("*"));
        filter.addFilter(new AcceptOnceFileListFilter<>());
        source.setFilter(filter);

        return source;
    }

    @Bean
    public IntegrationFlow mailFileFlow() {
        return IntegrationFlow
                .from(mailFileSource(),
                        c -> c.poller(Pollers.fixedDelay(5000).getObject()))
                .handle("emailService", "processMailFile")
                .get();
    }
}
