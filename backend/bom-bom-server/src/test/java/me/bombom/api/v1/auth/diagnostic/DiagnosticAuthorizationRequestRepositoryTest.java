package me.bombom.api.v1.auth.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class DiagnosticAuthorizationRequestRepositoryTest {
    private final OAuth2Diagnostics diagnostics = new OAuth2Diagnostics(Clock.systemUTC(), "shared-test-key",
            "SESSION", "instance-1", "release-1");
    private final DiagnosticAuthorizationRequestRepository repository = new DiagnosticAuthorizationRequestRepository(diagnostics);
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void tearDown() {
        diagnostics.close();
    }

    @Test
    void 같은_콜백이_반복되어도_시도를_연결하고_인증요청을_다시_사용하지_않는다() {
        MockHttpServletRequest request = request("secret-state");
        diagnostics.open(request);
        repository.saveAuthorizationRequest(authorization("secret-state"), request, response);
        String attempt = diagnostics.snapshot().get("attempt_id").toString();
        assertThat(repository.removeAuthorizationRequest(request, response)).isNotNull();
        assertThat(diagnostics.snapshot()).containsEntry("authorization_request_matched", true);
        diagnostics.close();
        diagnostics.open(request);
        assertThat(repository.removeAuthorizationRequest(request, response)).isNull();
        assertThat(diagnostics.snapshot()).containsEntry("attempt_id", attempt)
                .containsEntry("saved_attempt_present", false).containsEntry("authorization_request_matched", false);
        assertThat(diagnostics.snapshot().toString()).doesNotContain("secret-state");
    }

    @Test
    void state_불일치는_기존_인증요청을_소비하지_않는다() {
        MockHttpServletRequest request = request("wrong-state");
        diagnostics.open(request);
        repository.saveAuthorizationRequest(authorization("expected-state"), request, response);
        diagnostics.close();
        diagnostics.open(request);
        assertThat(repository.removeAuthorizationRequest(request, response)).isNull();
        assertThat(diagnostics.snapshot()).containsEntry("saved_attempt_present", true)
                .containsEntry("state_matches_saved", false).containsEntry("authorization_request_matched", false);
        request.setParameter("state", "expected-state");
        assertThat(repository.removeAuthorizationRequest(request, response)).isNotNull();
    }

    @Test
    void 세션이_없어도_이전_시도를_연결하고_새_세션을_만들지_않는다() {
        MockHttpServletRequest start = request("secret-state");
        diagnostics.open(start);
        repository.saveAuthorizationRequest(authorization("secret-state"), start, response);
        Object attempt = diagnostics.snapshot().get("attempt_id");
        diagnostics.close();
        MockHttpServletRequest callback = request("secret-state");
        diagnostics.open(callback);
        assertThat(repository.removeAuthorizationRequest(callback, response)).isNull();
        assertThat(callback.getSession(false)).isNull();
        assertThat(diagnostics.snapshot()).containsEntry("session_present", false).containsEntry("attempt_id", attempt)
                .doesNotContainKey("state_matches_saved");
    }

    @Test
    void 다른_로그인_시작이_기존_요청을_덮어쓰면_이전_시도를_남긴다() {
        MockHttpServletRequest request = request("first-state");
        diagnostics.open(request);
        repository.saveAuthorizationRequest(authorization("first-state"), request, response);
        Object previous = diagnostics.snapshot().get("attempt_id");
        repository.saveAuthorizationRequest(authorization("second-state"), request, response);
        assertThat(diagnostics.snapshot()).containsEntry("authorization_request_replaced", true)
                .containsEntry("previous_attempt_id", previous);
        request.setParameter("state", "first-state");
        assertThat(repository.removeAuthorizationRequest(request, response)).isNull();
        request.setParameter("state", "second-state");
        assertThat(repository.removeAuthorizationRequest(request, response)).isNotNull();
    }

    @Test
    void 지문_키가_바뀌면_state_불일치로_오진하지_않는다() {
        MockHttpServletRequest request = request("secret-state");
        diagnostics.open(request);
        repository.saveAuthorizationRequest(authorization("secret-state"), request, response);
        OAuth2Diagnostics other = new OAuth2Diagnostics(Clock.systemUTC(), "rotated-test-key", "SESSION", "instance-2", "release-2");
        try {
            other.open(request);
            var otherRepository = new DiagnosticAuthorizationRequestRepository(other);
            assertThat(otherRepository.removeAuthorizationRequest(request, response)).isNotNull();
            assertThat(other.snapshot()).containsEntry("authorization_request_matched", true)
                    .doesNotContainKey("state_matches_saved");
        } finally {
            other.close();
        }
    }

    @Test
    void 진단_세션_값은_이전_배포에_없는_클래스_없이_역직렬화된다() throws Exception {
        MockHttpServletRequest request = request("secret-state");
        diagnostics.open(request);
        repository.saveAuthorizationRequest(authorization("secret-state"), request, response);
        for (String name : java.util.Collections.list(request.getSession().getAttributeNames())) {
            if (!name.startsWith(DiagnosticAuthorizationRequestRepository.class.getName())) continue;
            var bytes = new java.io.ByteArrayOutputStream();
            try (var output = new java.io.ObjectOutputStream(bytes)) {
                output.writeObject(request.getSession().getAttribute(name));
            }
            try (var input = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())) {
                @Override
                protected Class<?> resolveClass(java.io.ObjectStreamClass descriptor) throws java.io.IOException, ClassNotFoundException {
                    if (descriptor.getName().startsWith("me.bombom.api.v1.auth.diagnostic")) {
                        throw new ClassNotFoundException("Previous deployment has no diagnostic classes");
                    }
                    return super.resolveClass(descriptor);
                }
            }) {
                assertThat(input.readObject()).isInstanceOf(java.util.Map.class);
            }
        }
    }

    private MockHttpServletRequest request(String state) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.setParameter("state", state);
        return request;
    }

    private OAuth2AuthorizationRequest authorization(String state) {
        return OAuth2AuthorizationRequest.authorizationCode().clientId("test-client")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth").state(state).build();
    }
}
