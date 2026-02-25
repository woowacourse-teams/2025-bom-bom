package me.bombom.api.v1.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import me.bombom.api.v1.auth.dto.response.LoadTestTokenResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoadTestTokenServiceTest {

    private static final String ADMIN_TOKEN = "loadtest-admin-token-v1";
    private static final String NEW_ADMIN_TOKEN = "loadtest-admin-token-v2";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private MemberRepository memberRepository;

    private LoadTestTokenService loadTestTokenService;
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        loadTestTokenService = new LoadTestTokenService(redisTemplate, memberRepository);
        ReflectionTestUtils.setField(loadTestTokenService, "loadTestEnabled", true);
        ReflectionTestUtils.setField(loadTestTokenService, "tokenPrefix", "loadtest:token");
        ReflectionTestUtils.setField(loadTestTokenService, "ttlSeconds", 60L);
        ReflectionTestUtils.setField(loadTestTokenService, "tokenHeaderName", "X-LoadTest-Token");
        ReflectionTestUtils.setField(loadTestTokenService, "issueAdminToken", ADMIN_TOKEN);
    }

    @Test
    void issueTokens_존재하지_않는_회원이_있으면_missingMemberIds_노출하지_않음() {
        Member existingMember = mock(Member.class);
        when(existingMember.getId()).thenReturn(1L);
        when(memberRepository.findAllById(Set.of(1L, 2L)))
                .thenReturn(List.of(existingMember));

        CIllegalArgumentException exception = (CIllegalArgumentException) assertThatThrownBy(
                () -> loadTestTokenService.issueTokens(List.of(1L, 2L), ADMIN_TOKEN)
        ).isInstanceOf(CIllegalArgumentException.class).actual();

        assertThat(exception.getErrorDetail()).isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
        assertThat(exception.getContext()).doesNotContainKey("missingMemberIds");
    }

    @Test
    void resolveMemberId는_관리자_토큰_회전_시_기존_토큰이_무효화됨() {
        Member member = buildMember(1L);
        when(memberRepository.findAllById(Set.of(1L))).thenReturn(List.of(member));

        LoadTestTokenResponse issued = loadTestTokenService.issueTokens(List.of(1L), ADMIN_TOKEN);
        String issuedToken = issued.results().get(0).token();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), any(Duration.class));
        String issuedKey = keyCaptor.getValue();

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String requestedKey = invocation.getArgument(0);
            if (issuedKey.equals(requestedKey)) {
                return valueCaptor.getValue();
            }
            return null;
        });

        assertThat(issuedToken).isNotBlank();
        assertThat(loadTestTokenService.resolveMemberId(issuedToken)).isEqualTo(1L);

        ReflectionTestUtils.setField(loadTestTokenService, "issueAdminToken", NEW_ADMIN_TOKEN);
        assertThat(loadTestTokenService.resolveMemberId(issuedToken)).isNull();

        assertThat(issuedKey)
                .as("토큰 저장 키는 로드테스트 토큰 네임스페이스를 사용한다")
                .startsWith("loadtest:token:");
    }

    private static Member buildMember(Long id) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        return member;
    }
}
