package me.bombom.api.v1.newsletterrequest.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsletterUrl {

    public static final int MAX_LENGTH = 512;

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final String DEFAULT_SCHEME_PREFIX = "https://";
    private static final String WWW_PREFIX = "www.";

    private final String original;
    private final String normalized;

    public static NewsletterUrl from(String rawUrl) {
        String normalized = normalize(rawUrl);
        if (normalized == null || rawUrl.strip().length() > MAX_LENGTH) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.DETAIL, "invalid newsletter url")
                    .addContext("url", rawUrl);
        }
        return new NewsletterUrl(rawUrl.strip(), normalized);
    }

    public static String normalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        URI uri = parse(rawUrl.strip());
        if (uri == null || !isValid(uri)) {
            return null;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.startsWith(WWW_PREFIX)) {
            host = host.substring(WWW_PREFIX.length());
        }
        String path = uri.getRawPath() == null ? "" : uri.getRawPath().toLowerCase(Locale.ROOT);
        return stripTrailingSlashes(host + path);
    }

    private static URI parse(String url) {
        String withScheme = url.contains("://") ? url : DEFAULT_SCHEME_PREFIX + url;
        try {
            return new URI(withScheme);
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private static boolean isValid(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        return scheme != null
                && ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))
                && host != null
                && host.contains(".");
    }

    private static String stripTrailingSlashes(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }
}
