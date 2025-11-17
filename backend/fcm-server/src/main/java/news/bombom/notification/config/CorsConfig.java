package news.bombom.notification.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String ALL_ORIGINS = "*";

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${cors.path-patterns:/api/**}")
    private String pathPatterns;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var corsRegistration = registry.addMapping(pathPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders(ALL_ORIGINS)
                .exposedHeaders(ALL_ORIGINS)
                .maxAge(maxAge);

        if (ALL_ORIGINS.equals(allowedOrigins)) {
            corsRegistration.allowedOriginPatterns(ALL_ORIGINS)
                    .allowCredentials(false);
            return;
        }
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        corsRegistration.allowedOrigins(origins.toArray(new String[0]))
                .allowCredentials(true);
    }

    /**
     * CORS 필터를 통한 추가 설정
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        if (ALL_ORIGINS.equals(allowedOrigins)) {
            config.setAllowCredentials(false);
            config.addAllowedOriginPattern(ALL_ORIGINS);
        } else {
            config.setAllowCredentials(true);
            Arrays.stream(allowedOrigins.split(","))
                    .filter(StringUtils::hasText)
                    .forEach(config::addAllowedOrigin);
        }

        config.addAllowedHeader(ALL_ORIGINS);
        config.addAllowedMethod(ALL_ORIGINS);
        config.setMaxAge(maxAge);

        source.registerCorsConfiguration(pathPatterns, config);
        return new CorsFilter(source);
    }
}
