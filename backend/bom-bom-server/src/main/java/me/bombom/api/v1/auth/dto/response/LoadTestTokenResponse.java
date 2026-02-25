package me.bombom.api.v1.auth.dto.response;

import java.util.List;

public record LoadTestTokenResponse(
    List<IssueResult> results,
    String tokenHeaderName,
    long ttlSeconds
) {
    public record IssueResult(Long memberId, String token) {}
}

