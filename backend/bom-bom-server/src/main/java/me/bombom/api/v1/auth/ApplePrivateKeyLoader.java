package me.bombom.api.v1.auth;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;

/**
 * Apple .p8 PEM 문자열을 ECPrivateKey로 변환하는 전용 로더.
 */
public class ApplePrivateKeyLoader {

    public ECPrivateKey loadFromPem(String privateKeyPem) {
        try {
            String normalized = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalized.getBytes(StandardCharsets.US_ASCII));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR);
        }
    }
}


