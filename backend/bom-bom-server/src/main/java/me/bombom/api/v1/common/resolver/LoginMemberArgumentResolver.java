package me.bombom.api.v1.common.resolver;

import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
        LoginMember loginMember = parameter.getParameterAnnotation(LoginMember.class);
        boolean anonymous = loginMember != null && loginMember.anonymous();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isAnonymous(authentication)) {
            if (anonymous) {
                return null;
            }
            throw new UnauthorizedException(ErrorDetail.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User) {
            Member member = ((CustomOAuth2User) principal).getMember();
            if (member == null) {
                throw new UnauthorizedException(ErrorDetail.UNAUTHORIZED);
            }
            return member;
        }
        throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN);
    }

    private boolean isAnonymous(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }
}
