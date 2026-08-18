package me.bombom.api.v1.auth.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

class AppleUserExchangeRestClientTest {

    private MockRestServiceServer server;
    private AppleUserExchangeRestClient client;

    @BeforeEach
    void setUp() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = generator.generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(privateKey.getEncoded())
                + "\n-----END PRIVATE KEY-----";

        AppleUserExchangeProperties properties =
                new AppleUserExchangeProperties("A1B2C3D4E5", "KEY123ID45", "com.example.app", pem, true);
        client = new AppleUserExchangeRestClient(builder, properties);
    }

    @Test
    void transfer_sub로_새_팀의_sub를_교환한다() {
        server.expect(requestTo("https://appleid.apple.com/auth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=client_credentials")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("scope=user.migration")))
                .andRespond(withSuccess("{\"access_token\":\"exchange-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://appleid.apple.com/auth/usermigrationinfo"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer exchange-token"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("transfer_sub=old-transfer-sub")))
                .andRespond(withSuccess("{\"sub\":\"new-team-sub\"}", MediaType.APPLICATION_JSON));

        String newSub = client.exchangeSub("old-transfer-sub");

        assertThat(newSub).isEqualTo("new-team-sub");
        server.verify();
    }

    @Test
    void Apple가_invalid_request를_반환하면_에러_바디를_포함해_예외를_던진다() {
        server.expect(requestTo("https://appleid.apple.com/auth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"exchange-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://appleid.apple.com/auth/usermigrationinfo"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_request\"}"));

        HttpClientErrorException exception = catchThrowableOfType(
                () -> client.exchangeSub("old-transfer-sub"), HttpClientErrorException.class);

        assertThat(exception.getResponseBodyAsString()).contains("invalid_request");
        server.verify();
    }
}
