package me.bombom.api.v1.auth.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import me.bombom.api.v1.auth.AppleClientSecretSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class AppleUserMigrationRestClientTest {

    @Mock
    private AppleClientSecretSupplier appleClientSecretSupplier;

    private MockRestServiceServer server;
    private AppleUserMigrationRestClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        given(appleClientSecretSupplier.generateFor("com.example.app")).willReturn("client-secret");
        client = new AppleUserMigrationRestClient(
                builder,
                appleClientSecretSupplier,
                new AppleUserMigrationProperties("A1B2C3D4E5", "com.example.app", true)
        );
    }

    @Test
    void 기존_sub와_수신_Team_ID로_transfer_sub를_발급한다() {
        server.expect(requestTo("https://appleid.apple.com/auth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=client_credentials")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("scope=user.migration")))
                .andRespond(withSuccess("{\"access_token\":\"migration-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://appleid.apple.com/auth/usermigrationinfo"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer migration-token"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sub=old-sub")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("target=A1B2C3D4E5")))
                .andRespond(withSuccess("{\"transfer_sub\":\"transfer-sub\"}", MediaType.APPLICATION_JSON));

        String transferSub = client.createTransferSub("old-sub");

        assertThat(transferSub).isEqualTo("transfer-sub");
        server.verify();
    }
}
