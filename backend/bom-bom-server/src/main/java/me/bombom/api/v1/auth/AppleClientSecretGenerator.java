package me.bombom.api.v1.auth;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Apple OAuth2 client_secret(JWT) 생성을 담당하는 생성기
 * 만들어진 JWT는 AppleOAuth2AccessTokenResponseClient 에서 Apple의 Refresh Token / Access Token 교환에 사용됨
 */
@Component
public class AppleClientSecretGenerator {

    private static final String APPLE_AUD = "https://appleid.apple.com";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final String teamId; //Apple Developer Team Id
    private final String keyId;
    private final ECPrivateKey privateKey; //Apple 에서 발급받은 Apple P8. yml에 있음

    public AppleClientSecretGenerator(
            @Value("${oauth2.apple.team-id}") String teamId,
            @Value("${oauth2.apple.key-id}") String keyId,
            ECPrivateKey privateKey
    ) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.privateKey = privateKey;
    }

    public String generateFor(String clientId) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plus(TOKEN_TTL);
            JWSHeader header = buildHeader();
            JWTClaimsSet claims = buildClaims(clientId, now, exp);
            return signAndSerialize(header, claims);
        } catch (Exception e) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR);
        }
    }

    private JWSHeader buildHeader() {
        return new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(keyId)
                .type(JOSEObjectType.JWT)
                .build();
    }

    private JWTClaimsSet buildClaims(String subjectClientId, Instant now, Instant exp) {
        return new JWTClaimsSet.Builder()
                .issuer(teamId)
                .subject(subjectClientId)
                .audience(APPLE_AUD)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .build();
    }

    private String signAndSerialize(JWSHeader header, JWTClaimsSet claims) throws Exception {
        SignedJWT signedJWT = new SignedJWT(header, claims);
        JWSSigner signer = new ECDSASigner(privateKey);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
