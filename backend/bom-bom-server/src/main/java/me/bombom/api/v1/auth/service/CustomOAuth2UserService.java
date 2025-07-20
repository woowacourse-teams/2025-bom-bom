package me.bombom.api.v1.auth.service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getAttribute("sub");
        String profileUrl = oAuth2User.getAttribute("picture");

        Optional<Member> member = memberRepository.findByProviderAndProviderId(provider, providerId);

        if (member.isEmpty()) {
            PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .profileUrl(profileUrl)
                    .build();
            session.setAttribute("pendingMember", pendingMember);
        }

        return new CustomOAuth2User(oAuth2User.getAttributes(), member.orElse(null));
    }
}
