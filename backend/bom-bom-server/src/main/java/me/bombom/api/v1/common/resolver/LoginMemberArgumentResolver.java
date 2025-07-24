package me.bombom.api.v1.common.resolver;

import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        log.info("========== LoginMemberArgumentResolver Start ==========");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||  !authentication.isAuthenticated()) {
            log.error("SecurityContextHolder에 인증 정보가 없거나, 인증되지 않은 사용자입니다.");
            throw new UnauthorizedException(ErrorDetail.UNAUTHORIZED);
        }
        log.info("Authentication 객체: {}", authentication);
        Object principal = authentication.getPrincipal();
        log.info("Principal 객체: {}", principal);
        if (principal instanceof CustomOAuth2User) {
            Member member = ((CustomOAuth2User) principal).getMember();
            if (member == null) {
                log.error("Principal 객체는 CustomOAuth2User이지만, 내부에 Member 객체가 null입니다.");
                throw new UnauthorizedException(ErrorDetail.UNAUTHORIZED);
            }
            log.info("성공적으로 Member 객체를 찾았습니다: {}", member);
            log.info("========== LoginMemberArgumentResolver End ==========");
            return member;
        }
        log.error("Principal 객체가 CustomOAuth2User 타입이 아닙니다. 타입: {}", principal.getClass().getName());
        throw new  UnauthorizedException(ErrorDetail.INVALID_TOKEN);
    }
}
