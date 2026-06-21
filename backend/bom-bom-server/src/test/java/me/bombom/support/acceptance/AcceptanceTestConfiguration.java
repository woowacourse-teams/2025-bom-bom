package me.bombom.support.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration(proxyBeanMethods = false)
public class AcceptanceTestConfiguration {

    @Bean
    AcceptanceDataSetLoader acceptanceDataSetLoader(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader
    ) {
        return new AcceptanceDataSetLoader(jdbcTemplate, objectMapper, resourceLoader);
    }

    @Bean
    AcceptanceTestAuthenticationFilter acceptanceTestAuthenticationFilter(MemberRepository memberRepository) {
        return new AcceptanceTestAuthenticationFilter(memberRepository);
    }

    @Bean
    FilterRegistrationBean<AcceptanceTestAuthenticationFilter> acceptanceTestAuthenticationFilterRegistration(
            AcceptanceTestAuthenticationFilter filter
    ) {
        FilterRegistrationBean<AcceptanceTestAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
