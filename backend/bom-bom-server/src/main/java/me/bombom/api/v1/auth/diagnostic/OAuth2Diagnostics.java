package me.bombom.api.v1.auth.diagnostic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/** Callback-scoped diagnostics. Only allowlisted fields are logged; context is cleared by the filter. */
@Slf4j
@Component
public class OAuth2Diagnostics {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern BEARER = Pattern.compile("(?i)^Bearer ([A-Za-z0-9._~+/-]+=*)$");
    private static final Set<String> ERROR_CODES = Set.of("invalid_user_info_response", "authorization_request_not_found",
            "invalid_token_response", "invalid_grant", "invalid_client", "invalid_request", "invalid_token",
            "invalid_state_parameter", "access_denied", "server_error", "temporarily_unavailable", "insufficient_scope");
    private static final Set<String> SCOPES = Set.of("openid", "profile", "email",
            "https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/userinfo.profile");
    private final ThreadLocal<Context> current = new ThreadLocal<>();
    private final Clock clock;
    private final String cookieName;
    private final String instance;

    public OAuth2Diagnostics(Clock clock,
            @Value("${server.servlet.session.cookie.name:JSESSIONID}") String cookieName,
            @Value("${HOSTNAME:unknown}") String instance) {
        this.clock = clock;
        this.cookieName = cookieName;
        this.instance = instance;
    }

    public void open(HttpServletRequest request) {
        current.set(new Context());
        update(context -> {
            Map<String, Object> fields = context.fields;
            fields.put("instance_id", bounded(instance, 128));
            fields.put("stage", "callback");
            fields.put("provider", provider(request.getRequestURI()));
            fields.put("session_present", request.getSession(false) != null);
            boolean cookiePresent = false;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookieName.equals(cookie.getName())) cookiePresent = true;
                }
            }
            fields.put("session_cookie_present", cookiePresent);
            String userAgent = request.getHeader("User-Agent");
            String agent = userAgent == null ? "" : userAgent.substring(0, Math.min(userAgent.length(), 1024));
            String platform = agent.contains("iPhone") || agent.contains("iPad") ? "ios"
                    : agent.contains("Android") ? "android" : "other";
            fields.put("client_platform", platform);
            fields.put("app_version", version(agent, "bombom/([0-9]+(?:\\.[0-9]+){0,3})"));
            fields.put("os_version", version(agent, "ios".equals(platform)
                    ? "(?:CPU (?:iPhone )?OS) ([0-9_]+)" : "Android ([0-9.]+)"));
        });
    }

    public void close() {
        current.remove();
    }

    Map<String, Object> snapshot() {
        Context context = current.get();
        return context == null ? Map.of() : Map.copyOf(context.fields);
    }

    void record(String name, Object value) {
        update(context -> {
            if (value == null) context.fields.remove(name);
            else context.fields.put(name, value);
        });
    }

    void userInfoToken(OAuth2AccessToken token) {
        update(context -> {
            context.token = token;
            context.fields.put("stage", "userinfo");
            context.fields.put("scopes", token.getScopes().stream().filter(SCOPES::contains).sorted().toList());
        });
    }

    public void outgoing(HttpRequest request) {
        update(context -> {
            Map<String, Object> fields = context.fields;
            fields.remove("token_matches_issued");
            List<String> values = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
            fields.put("authorization_present", !values.isEmpty());
            var matcher = BEARER.matcher(values.size() == 1 ? values.getFirst() : "");
            boolean valid = matcher.matches();
            fields.put("authorization_scheme_valid", valid);
            if (valid && context.token != null) {
                fields.put("token_matches_issued", context.token.getTokenValue().equals(matcher.group(1)));
            }
            if (context.token == null) return;
            Instant now = clock.instant();
            if (context.token.getIssuedAt() != null) {
                fields.put("token_age_ms", Duration.between(context.token.getIssuedAt(), now).toMillis());
            }
            if (context.token.getExpiresAt() != null) {
                fields.put("token_remaining_seconds", Duration.between(now, context.token.getExpiresAt()).getSeconds());
                fields.put("token_expired", !now.isBefore(context.token.getExpiresAt()));
            }
        });
    }

    public ClientHttpRequestInterceptor interceptor() {
        return (request, body, execution) -> {
            update(context -> {
                context.fields.put("userinfo_host", request.getURI().getHost());
                context.fields.put("userinfo_path", request.getURI().getPath());
            });
            outgoing(request);
            long started = System.nanoTime();
            try {
                var response = execution.execute(request, body);
                try {
                    record("userinfo_status", response.getStatusCode().value());
                } catch (Exception ignored) {
                    record("diagnostic_error", true);
                }
                return response;
            } finally {
                record("userinfo_duration_ms", (System.nanoTime() - started) / 1_000_000);
            }
        };
    }

    public void failureDetails(Throwable failure) {
        update(context -> {
            String code = failure instanceof OAuth2AuthenticationException auth ? auth.getError().getErrorCode()
                    : failure instanceof OAuth2AuthorizationException auth ? auth.getError().getErrorCode() : "other";
            context.fields.put("error_code", knownError(code));
            context.fields.put("exception_type", failure.getClass().getSimpleName());
            Throwable cause = failure;
            for (int depth = 0; cause != null && depth < 8; depth++, cause = cause.getCause()) {
                if (cause instanceof RestClientResponseException response) {
                    context.fields.put("provider_status", response.getStatusCode().value());
                    String body = response.getResponseBodyAsString();
                    if (body.length() <= 4096) {
                        try {
                            JsonNode json = JSON.readTree(body);
                            if (json != null) {
                                context.fields.put("provider_error", knownError(json.path("error").asText()));
                                // Never log arbitrary provider descriptions: they can echo tokens or user data.
                                context.fields.put("provider_description", "Invalid Credentials".equals(json.path("error_description").asText())
                                        ? "Invalid Credentials" : "redacted");
                            }
                        } catch (Exception ignored) {
                            context.fields.put("provider_body_format", "unparsed");
                        }
                    }
                    break;
                }
            }
        });
    }

    public void failed(HttpServletRequest request, Throwable failure) {
        boolean standalone = current.get() == null;
        try {
            if (standalone) open(request);
            failureDetails(failure);
            update(context -> {
                Span.current().setAttribute("auth.outcome", "failure");
                Span.current().setStatus(StatusCode.ERROR);
            });
            emitFailure();
        } finally {
            if (standalone) close();
        }
    }

    private void emitFailure() {
        update(context -> {
            Map<String, Object> fields = new LinkedHashMap<>(snapshot());
            fields.put("event", "oauth_login_failed");
            fields.put("message", "OAuth2 로그인 실패");
            var span = Span.current().getSpanContext();
            fields.put("trace_id", span.getTraceId());
            fields.put("span_id", span.getSpanId());
            fields.put("trace_sampled", span.isSampled());
            String message;
            try {
                message = JSON.writeValueAsString(fields);
            } catch (Exception ignored) {
                message = "{\"event\":\"oauth_diagnostic_error\"}";
            }
            log.warn("{}", message);
        });
    }

    private void update(Consumer<Context> action) {
        Context context = current.get();
        if (context == null) return;
        try {
            action.accept(context);
        } catch (RuntimeException ignored) {
            context.fields.put("diagnostic_error", true);
        }
    }

    private static String knownError(String code) {
        return ERROR_CODES.contains(code) ? code : "other";
    }

    private static String provider(String path) {
        return path.endsWith("/google") ? "google" : path.endsWith("/apple") ? "apple" : "other";
    }

    private static String bounded(String value, int length) {
        String sanitized = value.replaceAll("[^A-Za-z0-9._:/@-]", "_");
        return sanitized.substring(0, Math.min(sanitized.length(), length));
    }

    private static String version(String agent, String pattern) {
        var matcher = Pattern.compile(pattern).matcher(agent);
        return matcher.find() ? matcher.group(1).replace('_', '.').substring(0, Math.min(matcher.group(1).length(), 32)) : "unknown";
    }

    private static final class Context {
        private final Map<String, Object> fields = new LinkedHashMap<>();
        private OAuth2AccessToken token;
    }
}
