package news.bombom.notification.config;

import java.util.Locale;
import news.bombom.notification.domain.NotificationCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NotificationCategoryConverter implements Converter<String, NotificationCategory> {

    @Override
    public NotificationCategory convert(String source) {
        String normalized = source.trim()
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
        return NotificationCategory.valueOf(normalized);
    }
}
