package me.bombom.api.v1.common.config;

import jakarta.servlet.DispatcherType;
import java.util.List;
import me.bombom.api.log.MDCLoggingFilter;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<MDCLoggingFilter> mdcLoggingFilterRegistration() {
        FilterRegistrationBean<MDCLoggingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new MDCLoggingFilter());
        reg.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }
}
