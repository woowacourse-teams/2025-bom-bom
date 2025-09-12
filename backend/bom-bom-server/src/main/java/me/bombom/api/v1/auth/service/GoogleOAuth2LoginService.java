package me.bombom.api.v1.auth.service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Google OAuth2 로그인 서비스
 * Google의 표준 OAuth2 플로우를 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleOAuth2LoginService implements OAuth2LoginService {

    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    private final MemberRepository memberRepository;
    private final HttpSession session;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Google OAuth2 로그인 처리 시작");
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        String providerId = oAuth2User.getAttribute("sub");
        String profileUrl = oAuth2User.getAttribute("picture");

        // 기존 회원 확인
        Optional<Member> member = memberRepository.findByProviderAndProviderId("google", providerId);

        if (member.isEmpty()) {
            PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                    .provider("google")
                    .providerId(providerId)
                    .profileUrl(profileUrl)
                    .build();
            session.setAttribute("pendingMember", pendingMember);
            log.info("Google 신규 회원 - 회원가입 대기 상태로 설정");
        } else {
            log.info("Google 기존 회원 - 로그인 성공, memberId: {}", member.get().getId());
        }
        return new CustomOAuth2User(oAuth2User.getAttributes(), member.orElse(null), null, null);
    }

    @Override
    public String getProviderType() {
        return "google";
    }

    @Override
    public boolean supports(String provider) {
        return "google".equals(provider);
    }
}
