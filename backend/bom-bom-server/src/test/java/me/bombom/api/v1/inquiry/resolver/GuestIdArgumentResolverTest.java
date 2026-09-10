package me.bombom.api.v1.inquiry.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class GuestIdArgumentResolverTest {

    private final GuestIdArgumentResolver resolver = new GuestIdArgumentResolver();

    @Mock
    private NativeWebRequest webRequest;

    @Test
    void X_Guest_Id_헤더값을_그대로_반환한다() {
        when(webRequest.getHeader("X-Guest-Id")).thenReturn("guest-uuid-1234");

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        assertThat(result).isEqualTo("guest-uuid-1234");
    }

    @Test
    void 헤더가_없으면_null을_반환한다() {
        when(webRequest.getHeader("X-Guest-Id")).thenReturn(null);

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        assertThat(result).isNull();
    }
}
