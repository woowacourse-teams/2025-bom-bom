package me.bombom.api.v1.auth.service;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.response.LoadTestTokenResponse;
import me.bombom.api.v1.auth.dto.response.LoadTestTokenResponse.IssueResult;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoadTestTokenService {

    private static final String TOKEN_KEY_PREFIX = "loadtest:token:";

    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    @Value("${loadtest.auth.enabled:false}")
    private boolean loadTestEnabled;

    @Value("${loadtest.token.prefix:" + TOKEN_KEY_PREFIX + "}")
    private String tokenPrefix;

    @Value("${loadtest.token.ttl-seconds:3600}")
    private long ttlSeconds;

    @Value("${loadtest.auth.header-name:X-LoadTest-Token}")
    private String tokenHeaderName;

    @Value("${loadtest.auth.issue-admin-token:}")
    private String issueAdminToken;

    public LoadTestTokenResponse issueTokens(List<Long> memberIds, String adminToken) {
        if (!loadTestEnabled) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext("feature", "loadtest");
        }
        validateIssueRequest(adminToken);

        if (memberIds == null || memberIds.isEmpty()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_BODY_VALIDATION)
                .addContext("field", "memberIds");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(memberIds);
        Set<Long> existingIds = new HashSet<>(memberRepository.findAllById(uniqueIds).stream()
            .map(member -> member.getId())
            .toList());

        if (existingIds.size() != uniqueIds.size()) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("feature", "loadtest.issue-endpoint");
        }

        List<IssueResult> results = new ArrayList<>();
        for (Long memberId : uniqueIds) {
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(
                generateTokenKey(token),
                String.valueOf(memberId),
                Duration.ofSeconds(ttlSeconds)
            );
            results.add(new IssueResult(memberId, token));
        }
        return new LoadTestTokenResponse(Collections.unmodifiableList(results), tokenHeaderName, ttlSeconds);
    }

    private void validateIssueRequest(String adminToken) {
        if (!isAuthorizedAdminRequest(adminToken)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext("feature", "loadtest.issue-endpoint");
        }
    }

    private boolean isAuthorizedAdminRequest(String adminToken) {
        if (issueAdminToken == null || issueAdminToken.isBlank()) {
            return false;
        }
        if (adminToken == null || adminToken.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
            issueAdminToken.getBytes(StandardCharsets.UTF_8),
            adminToken.getBytes(StandardCharsets.UTF_8)
        );
    }

    public Long resolveMemberId(String token) {
        if (!loadTestEnabled || token == null || token.isBlank()) {
            return null;
        }
        String key = generateTokenKey(token);
        String memberId = redisTemplate.opsForValue().get(key);
        if (memberId == null || memberId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(memberId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String tokenScope() {
        if (issueAdminToken == null || issueAdminToken.isBlank()) {
            return "disabled";
        }
        return hashIssueAdminToken();
    }

    private String generateTokenKey(String token) {
        return normalizePrefix() + tokenScope() + ":" + token;
    }

    private String normalizePrefix() {
        if (tokenPrefix.endsWith(":")) {
            return tokenPrefix;
        }
        return tokenPrefix + ":";
    }

    private String hashIssueAdminToken() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(issueAdminToken.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate loadtest token scope");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String part = Integer.toHexString(b & 0xff);
            if (part.length() == 1) {
                hex.append('0');
            }
            hex.append(part);
        }
        return hex.toString();
    }
}
