package me.bombom.api.v1.inquiry.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class GuestIdArgumentResolverTest {

    private final GuestIdArgumentResolver resolver = new GuestIdArgumentResolver();

    @Mock
    private NativeWebRequest webRequest;

    @Test
    void UUID_v4_형식의_X_Guest_Id_헤더값을_그대로_반환한다() {
        when(webRequest.getHeader("X-Guest-Id")).thenReturn("e4560796-3db9-4f26-828a-c356c91d5076");

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        assertThat(result).isEqualTo("e4560796-3db9-4f26-828a-c356c91d5076");
    }

    @Test
    void 대문자_UUID_v4도_허용한다() {
        when(webRequest.getHeader("X-Guest-Id")).thenReturn("E4560796-3DB9-4F26-828A-C356C91D5076");

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        assertThat(result).isEqualTo("E4560796-3DB9-4F26-828A-C356C91D5076");
    }

    @Test
    void 헤더가_없으면_null을_반환한다() {
        when(webRequest.getHeader("X-Guest-Id")).thenReturn(null);

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        assertThat(result).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void 빈_문자열이거나_공백이면_예외를_던진다(String blankValue) {
        when(webRequest.getHeader("X-Guest-Id")).thenReturn(blankValue);

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> assertThat(((CIllegalArgumentException) e).getErrorDetail())
                        .isEqualTo(ErrorDetail.BLANK_NOT_ALLOWED));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "guest-uuid-1",
            "not-a-uuid",
            "e4560796-3db9-4f26-828a-c356c91d507",
            "e4560796-3db9-4f26-828a-c356c91d50766",
            "e4560796-3db9-1f26-828a-c356c91d5076",
            "e4560796-3db9-4f26-c28a-c356c91d5076",
            "g4560796-3db9-4f26-828a-c356c91d5076"
    })
    void UUID_v4_형식이_아니면_예외를_던진다(String invalidGuestId) {
        when(webRequest.getHeader("X-Guest-Id")).thenReturn(invalidGuestId);

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(CIllegalArgumentException.class)
                .satisfies(e -> assertThat(((CIllegalArgumentException) e).getErrorDetail())
                        .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE));
    }
}
