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
import java.util.function.Supplier;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;

public class AppleClientSecretSupplier implements Supplier<String> {

    private static final String APPLE_AUD = "https://appleid.apple.com";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final String teamId; //Apple Developer Team Id
    private final String keyId; //kid
    private final String clientId; //sub (default)
    private final ECPrivateKey privateKey;

    public AppleClientSecretSupplier(String teamId, String keyId, String clientId, ECPrivateKey privateKey) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.clientId = clientId;
        this.privateKey = privateKey;
    }

    @Override
    public String get() {
        return signForClientId(this.clientId);
    }

    // Generate client secret for a specific clientId (e.g., Bundle ID for native)
    public String generateFor(String targetClientId) {
        return signForClientId(targetClientId);
    }

    private String signForClientId(String subjectClientId) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plus(TOKEN_TTL);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(keyId)
                    .type(JOSEObjectType.JWT)
                    .build();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .subject(subjectClientId)
                    .audience(APPLE_AUD)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new ECDSASigner(privateKey);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR);
        }
    }
}
