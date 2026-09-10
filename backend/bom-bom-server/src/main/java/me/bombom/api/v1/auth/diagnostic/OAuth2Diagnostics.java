package me.bombom.api.v1.auth.diagnostic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/** Request-thread scoped, allowlisted diagnostics. Never retain raw tokens in diagnostic context. */
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
    private final byte[] key;
    private final String keyScope;
    private final String cookieName;
    private final String instance;
    private final String release;

    public OAuth2Diagnostics(Clock clock,
            @Value("${oauth.diagnostics.hmac-key:}") String hmacKey,
            @Value("${server.servlet.session.cookie.name:JSESSIONID}") String cookieName,
            @Value("${APP_INSTANCE_ID:${HOSTNAME:unknown}}") String instance,
            @Value("${APP_RELEASE:unknown}") String release) {
        this.clock = clock;
        this.cookieName = cookieName;
        this.instance = instance;
        this.release = release;
        this.keyScope = hmacKey.isBlank() ? "process" : "shared";
        this.key = hmacKey.isBlank() ? new SecureRandom().generateSeed(32) : hmacKey.getBytes(StandardCharsets.UTF_8);
    }

    public void open(HttpServletRequest request) {
        current.set(new Context());
        update(context -> {
            Map<String, Object> fields = context.fields;
            fields.put("fingerprint_scope", keyScope);
            fields.put("fingerprint_key_id", fingerprint("key", "v1"));
            fields.put("instance_id", bounded(instance, 128));
            fields.put("release", bounded(release, 256));
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
            String state = request.getParameter("state");
            if (state != null && state.length() <= 4096) fields.put("attempt_id", fingerprint("state", state));
        });
    }

    public void close() {
        current.remove();
    }

    public void attempt(String state) {
        update(context -> {
            if (state != null && state.length() <= 4096) context.fields.put("attempt_id", fingerprint("state", state));
        });
    }

    String stateFingerprint(String state) {
        try {
            return state == null || state.length() > 4096 ? null : fingerprint("state", state);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public Map<String, Object> snapshot() {
        Context context = current.get();
        return context == null ? Map.of() : Map.copyOf(context.fields);
    }

    void record(String name, Object value) {
        update(context -> {
            if (value == null) context.fields.remove(name);
            else context.fields.put(name, value);
        });
    }

    public void tokenReceived(OAuth2AccessTokenResponse response) {
        update(context -> {
            var token = response.getAccessToken();
            context.receivedAt = clock.instant();
            context.fields.put("issued_token_fingerprint", fingerprint("token", token.getTokenValue()));
            context.fields.put("token_received_at", context.receivedAt.toString());
            if (token.getIssuedAt() != null && token.getExpiresAt() != null) {
                long lifetime = Duration.between(token.getIssuedAt(), token.getExpiresAt()).getSeconds();
                context.expiresAt = context.receivedAt.plusSeconds(lifetime);
                context.fields.put("token_lifetime_seconds", lifetime);
            }
            context.fields.put("scopes", token.getScopes().stream().filter(SCOPES::contains).sorted().toList());
            context.fields.put("token_exchange_success", true);
        });
        emit("oauth_token_received", false);
    }

    public void outgoing(HttpRequest request) {
        update(context -> {
            Map<String, Object> fields = context.fields;
            fields.remove("sent_token_fingerprint");
            fields.remove("token_matches_issued");
            List<String> values = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
            fields.put("authorization_present", !values.isEmpty());
            var matcher = BEARER.matcher(values.size() == 1 ? values.getFirst() : "");
            boolean valid = matcher.matches();
            fields.put("authorization_scheme_valid", valid);
            if (valid) {
                String sent = fingerprint("token", matcher.group(1));
                fields.put("sent_token_fingerprint", sent);
                if (fields.containsKey("issued_token_fingerprint")) {
                    fields.put("token_matches_issued", sent.equals(fields.get("issued_token_fingerprint")));
                }
            }
            Instant now = clock.instant();
            if (context.receivedAt != null) fields.put("token_age_ms", Duration.between(context.receivedAt, now).toMillis());
            if (context.expiresAt != null) {
                fields.put("token_remaining_seconds", Duration.between(now, context.expiresAt).getSeconds());
                fields.put("token_expired", !now.isBefore(context.expiresAt));
            }
        });
    }

    public ClientHttpRequestInterceptor interceptor(String stage) {
        return (request, body, execution) -> {
            record("stage", stage);
            record(stage + "_host", request.getURI().getHost());
            record(stage + "_path", request.getURI().getPath());
            if ("userinfo".equals(stage)) outgoing(request);
            long started = System.nanoTime();
            try {
                var response = execution.execute(request, body);
                // A diagnostics read must not close, consume, or replace the actual response.
                try {
                    record(stage + "_status", response.getStatusCode().value());
                    for (String header : List.of("x-request-id", "x-goog-request-id")) {
                        String requestId = response.getHeaders().getFirst(header);
                        if (requestId != null && requestId.matches("[A-Za-z0-9_-]{8,128}")) {
                            record(stage + "_request_id", requestId);
                            break;
                        }
                    }
                    String challenge = response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);
                    if (challenge != null && challenge.length() <= 1024) {
                        var matcher = Pattern.compile("error=\"([a-z_]+)\"").matcher(challenge);
                        if (matcher.find()) record(stage + "_auth_error", knownError(matcher.group(1)));
                    }
                } catch (Exception ignored) {
                    record("diagnostic_error", true);
                }
                return response;
            } finally {
                record(stage + "_duration_ms", (System.nanoTime() - started) / 1_000_000);
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
            failed(failure);
        } finally {
            if (standalone) close();
        }
    }

    public void failed(Throwable failure) {
        failureDetails(failure);
        update(context -> {
            Span.current().setAttribute("auth.outcome", "failure");
            Span.current().setStatus(StatusCode.ERROR);
        });
        emit("oauth_login_failed", true);
    }

    public void emit(String event, boolean warning) {
        update(context -> {
            Map<String, Object> fields = new LinkedHashMap<>(context.fields);
            fields.put("event", event);
            if ("oauth_login_failed".equals(event)) fields.put("message", "OAuth2 로그인 실패");
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
            if (warning) log.warn("{}", message);
            else log.info("{}", message);
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

    private String fingerprint(String purpose, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            mac.update((purpose + "\0").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)), 0, 16);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("OAuth diagnostic fingerprint unavailable", e);
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
        private Instant receivedAt;
        private Instant expiresAt;
    }
}
