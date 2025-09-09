    package me.bombom.api.v1.auth.service;

    import com.nimbusds.jwt.JWT;
    import com.nimbusds.jwt.JWTParser;
    import jakarta.servlet.http.HttpSession;
    import java.util.Map;
    import java.util.Optional;
    import lombok.RequiredArgsConstructor;
    import me.bombom.api.v1.auth.dto.CustomOAuth2User;
    import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
    import me.bombom.api.v1.auth.enums.OAuth2Provider;
    import me.bombom.api.v1.common.exception.ErrorDetail;
    import me.bombom.api.v1.common.exception.UnauthorizedException;
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

        private static final String ID_TOKEN_KEY = "id_token";

        private final MemberRepository memberRepository;
        private final HttpSession session;

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
            String provider = userRequest.getClientRegistration().getRegistrationId();
            OAuth2Provider oAuth2Provider = OAuth2Provider.from(provider);
            
            Map<String, Object> attributes;
            String providerId;
            String profileUrl = "";
            
            if (oAuth2Provider == OAuth2Provider.APPLE) {
                // Apple의 경우 ID Token에서 사용자 정보 추출
                try {
                    String idToken = userRequest.getAdditionalParameters().get(ID_TOKEN_KEY).toString();
                    JWT jwt = JWTParser.parse(idToken);
                    attributes = jwt.getJWTClaimsSet().getClaims();
                    providerId = (String) attributes.get(oAuth2Provider.getIdKey());
                } catch (Exception e) {
                    throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN);
                }
            } else {
                // Google 등 다른 제공자는 기본 OAuth2UserService 사용
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
                PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .profileUrl(profileUrl)
                        .build();
                session.setAttribute("pendingMember", pendingMember);
            }

            return new CustomOAuth2User(attributes, member.orElse(null));
        }
    }
