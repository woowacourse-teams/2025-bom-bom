package me.bombom.api.v1.auth.service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final MemberRepository memberRepository;
    private final HttpSession session;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2ProviderInfo oAuth2Provider = OAuth2ProviderInfo.from(provider);
        String providerId = oAuth2User.getAttribute(oAuth2Provider.getIdKey());
        String profileUrl = oAuth2User.getAttribute(oAuth2Provider.getProfileImageKey());

        Optional<Member> member = memberRepository.findByProviderAndProviderIdIncludeDeleted(provider, providerId);

        if (member.isPresent() && member.get().isWithdrawnMember()) {
            throw new UnauthorizedException(ErrorDetail.WITHDRAWN_MEMBER);
        }

        if (member.isEmpty()) {
            // Apple 로그인인 경우에만 Refresh Token 추출
            String refreshToken = (oAuth2Provider == OAuth2ProviderInfo.APPLE) 
                ? extractRefreshToken(userRequest) : null;
            PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .profileUrl(profileUrl)
                    .appleRefreshToken(refreshToken)
                    .build();
            session.setAttribute("pendingMember", pendingMember);
        } else {
            // 기존 회원의 경우 Apple Refresh Token 업데이트
            updateRefreshTokenIfNeeded(member.get(), userRequest);
        }

        return new CustomOAuth2User(oAuth2User.getAttributes(), member.orElse(null));
    }

    private String extractRefreshToken(OAuth2UserRequest userRequest) {
        try {
            Object refreshTokenObj = userRequest.getAdditionalParameters().get(REFRESH_TOKEN_KEY);
            return refreshTokenObj != null ? refreshTokenObj.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateRefreshTokenIfNeeded(Member member, OAuth2UserRequest userRequest) {
        try {
            String refreshToken = extractRefreshToken(userRequest);
            
            if (refreshToken != null && member.getProvider().equals(OAuth2ProviderInfo.APPLE.getProvider())) {
                if (!refreshToken.equals(member.getAppleRefreshToken())) {
                    member.updateAppleRefreshToken(refreshToken);
                    memberRepository.save(member);
                }
            }
        } catch (Exception e) {
            // 실패해도 로그인은 계속 진행
        }
    }
}
