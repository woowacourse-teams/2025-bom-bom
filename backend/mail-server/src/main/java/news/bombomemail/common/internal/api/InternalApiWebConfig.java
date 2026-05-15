package news.bombomemail.common.internal.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InternalApiWebConfig implements WebMvcConfigurer {

    private final InternalApiKeyInterceptor internalApiKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalApiKeyInterceptor)
                .addPathPatterns("/internal/**");
    }
}
