package me.bombom.api.v1.common.config;

import jakarta.servlet.DispatcherType;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.log.MDCLoggingFilter;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import me.bombom.api.v1.common.session.SessionCookieRefreshInterceptor;
import me.bombom.api.v1.inquiry.resolver.GuestIdArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private static final Duration SESSION_COOKIE_REFRESH_INTERVAL = Duration.ofDays(1);

    private final CookieSerializer cookieSerializer;
    private final Clock clock;

    @Value("${server.servlet.session.cookie.name:JSESSIONID_PROD}")
    private String sessionCookieName;

    @Value("${server.servlet.session.cookie.domain:}")
    private String sessionCookieDomain;

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
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionCookieRefreshInterceptor(
                cookieSerializer,
                clock,
                SESSION_COOKIE_REFRESH_INTERVAL
        ));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(sessionCookieName, sessionCookieDomain));
        resolvers.add(new GuestIdArgumentResolver());
    }
}
