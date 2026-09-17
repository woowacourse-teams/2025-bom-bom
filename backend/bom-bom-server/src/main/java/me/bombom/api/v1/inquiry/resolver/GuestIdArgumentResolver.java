package me.bombom.api.v1.inquiry.resolver;

import java.util.regex.Pattern;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class GuestIdArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String GUEST_ID_HEADER = "X-Guest-Id";

    private static final Pattern UUID_V4_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(GuestId.class)
                && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        String guestId = webRequest.getHeader(GUEST_ID_HEADER);
        if (guestId == null) {
            // 헤더가 없는 경우는 회원 요청일 수 있으므로 통과시키고, memberId/guestId 존재 여부는 서비스 레이어에서 검증한다.
            return null;
        }

        if (guestId.isBlank()) {
            throw new CIllegalArgumentException(ErrorDetail.BLANK_NOT_ALLOWED)
                    .addContext("header", GUEST_ID_HEADER);
        }

        if (!UUID_V4_PATTERN.matcher(guestId).matches()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("header", GUEST_ID_HEADER)
                    .addContext("reason", "guest_id_must_be_uuid_v4");
        }

        return guestId;
    }
}
