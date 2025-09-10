package me.bombom.api.v1.auth.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String ID_TOKEN_KEY = "id_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final MemberRepository memberRepository;
    private final HttpSession session;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2ProviderInfo oAuth2Provider = OAuth2ProviderInfo.from(provider);

        Map<String, Object> attributes;
        String providerId;
        String profileUrl = "";

        if (oAuth2Provider == OAuth2ProviderInfo.APPLE) {
            try {
                // Apple의 경우 ID Token을 additionalParameters에서 찾기
                Object idTokenObj = userRequest.getAdditionalParameters().get(ID_TOKEN_KEY);
                if (idTokenObj == null) {
                    log.error("Apple ID Token이 additionalParameters에 없음 - available keys: {}", userRequest.getAdditionalParameters().keySet());
                    throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                        .addContext("provider", provider)
                        .addContext("reason", "apple_id_token_not_found_in_additional_parameters");
                }
                String idToken = idTokenObj.toString();
                JWT jwt = JWTParser.parse(idToken);
                attributes = jwt.getJWTClaimsSet().getClaims();
                providerId = (String) attributes.get(oAuth2Provider.getIdKey());
                log.info("Apple ID Token 파싱 성공 - providerId: {}", providerId);
            } catch (Exception e) {
                log.error("Apple ID Token 파싱 실패 - error: {}", e.getMessage(), e);
                throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("provider", provider)
                    .addContext("reason", "apple_id_token_parse_failed");
            }
        } else {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            attributes = oAuth2User.getAttributes();
            providerId = oAuth2User.getAttribute(oAuth2Provider.getIdKey());
            profileUrl = oAuth2User.getAttribute(oAuth2Provider.getProfileImageKey());
        }

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
            updateRefreshTokenIfNeeded(member.get(), userRequest);
        }

        return new CustomOAuth2User(attributes, member.orElse(null));
    }

    private String extractRefreshToken(OAuth2UserRequest userRequest) {
        try {
            Object refreshTokenObj = userRequest.getAdditionalParameters().get(REFRESH_TOKEN_KEY);
            if (refreshTokenObj != null) {
                log.info("Apple Refresh Token 추출 성공");
            }
            return refreshTokenObj != null ? refreshTokenObj.toString() : null;
        } catch (Exception e) {
            log.warn("Apple Refresh Token 추출 실패 - error: {}", e.getMessage());
            return null;
        }
    }

    private void updateRefreshTokenIfNeeded(Member member, OAuth2UserRequest userRequest) {
        try {
            String refreshToken = extractRefreshToken(userRequest);
            
            if (refreshToken != null && member.getProvider().equals(OAuth2ProviderInfo.APPLE.getProvider())) {
                if (!refreshToken.equals(member.getAppleRefreshToken())) {
                    log.info("Apple Refresh Token 업데이트 - memberId: {}", member.getId());
                    member.updateAppleRefreshToken(refreshToken);
                    memberRepository.save(member);
                }
            }
        } catch (Exception e) {
            log.warn("Apple Refresh Token 업데이트 실패 - memberId: {}, error: {}", member.getId(), e.getMessage());
        }
    }
}
