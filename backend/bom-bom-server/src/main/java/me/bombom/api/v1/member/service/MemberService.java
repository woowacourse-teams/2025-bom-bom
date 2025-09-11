package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.enums.DuplicateCheckField;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;
import me.bombom.api.v1.member.event.MemberSignupEvent;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final long MEMBER_ROLE_ID = 1L;

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Member signup(PendingOAuth2Member pendingMember, MemberSignupRequest signupRequest) {
        validateDuplicateNickname(signupRequest.nickname());
        validateDuplicateEmail(signupRequest.email());

        System.out.println("=== 회원가입 시 Refresh Token 저장 ===");
        System.out.println("provider: " + pendingMember.getProvider());
        System.out.println("providerId: " + pendingMember.getProviderId());
        System.out.println("appleRefreshToken: " + (pendingMember.getAppleRefreshToken() != null ? "있음" : "없음"));

        Member newMember = Member.builder()
                .provider(pendingMember.getProvider())
                .providerId(pendingMember.getProviderId())
                .email(signupRequest.email())
                .profileImageUrl(pendingMember.getProfileUrl())
                .nickname(signupRequest.nickname())
                .gender(signupRequest.gender())
                .roleId(MEMBER_ROLE_ID)
                .birthDate(signupRequest.birthDate())
                .appleRefreshToken(pendingMember.getAppleRefreshToken())
                .build();
        Member savedMember = memberRepository.save(newMember);
        System.out.println("회원가입 완료 - memberId: " + savedMember.getId() + ", appleRefreshToken: " + 
                (savedMember.getAppleRefreshToken() != null ? "저장됨" : "없음"));
        
        applicationEventPublisher.publishEvent(new MemberSignupEvent(savedMember.getId()));
        return savedMember;
    }

    public boolean checkSignupDuplicate(DuplicateCheckField field, String value) {
        return switch (field) {
            case NICKNAME -> memberRepository.existsByNickname(value);
            case EMAIL -> memberRepository.existsByEmail(value);
        };
    }

    public MemberProfileResponse getProfile(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, id)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "member")
                );
        return MemberProfileResponse.from(member);
    }

    @Transactional
    public void revoke(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "member")
            );
//        OAuth2ProviderInfo providerType = OAuth2ProviderInfo.fromCode(member.getProvider());
//        OAuth2Provider provider = providerFactory.getProvider(providerType);
//        provider.processWithdrawal(member);
        memberRepository.delete(member);
    }

    @Transactional
    public Member findOrCreateMemberByAppleId(String appleId, String email, String name) {
        return findOrCreateMemberByAppleId(appleId, email, name, null);
    }

    @Transactional
    public Member findOrCreateMemberByAppleId(String appleId, String email, String name, String refreshToken) {
        System.out.println("=== Apple ID로 회원 조회/생성 ===");
        System.out.println("appleId: " + appleId);
        System.out.println("email: " + email);
        System.out.println("name: " + name);
        System.out.println("refreshToken: " + (refreshToken != null ? "있음" : "없음"));
        
        // Apple ID로 기존 회원 조회
        return memberRepository.findByProviderAndProviderId("apple", appleId)
                .map(existingMember -> {
                    System.out.println("기존 회원 발견 - memberId: " + existingMember.getId());
                    
                    // Refresh Token이 있으면 업데이트
                    if (refreshToken != null && !refreshToken.equals(existingMember.getAppleRefreshToken())) {
                        System.out.println("Apple Refresh Token 업데이트");
                        existingMember.updateAppleRefreshToken(refreshToken);
                        Member updatedMember = memberRepository.save(existingMember);
                        System.out.println("Apple Refresh Token 업데이트 완료");
                        return updatedMember;
                    }
                    
                    return existingMember;
                })
                .orElseGet(() -> {
                    System.out.println("기존 회원 없음 - 새 회원 생성");
                    
                    // Apple ID Token에서 이메일이 제공되지 않은 경우 처리
                    String memberEmail = email != null ? email : appleId + "@apple.privaterelay.appleid.com";
                    
                    Member newMember = Member.builder()
                            .provider("apple")
                            .providerId(appleId)
                            .email(memberEmail)
                            .nickname(name != null ? name : "Apple User")
                            .roleId(MEMBER_ROLE_ID)
                            .appleRefreshToken(refreshToken)
                            .build();
                    
                    Member savedMember = memberRepository.save(newMember);
                    System.out.println("새 회원 생성 완료 - memberId: " + savedMember.getId());
                    System.out.println("새 회원 Apple Refresh Token: " + (savedMember.getAppleRefreshToken() != null ? "저장됨" : "없음"));
                    return savedMember;
                });
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATE_EMAIL)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "email")
                    .addContext(ErrorContextKeys.OPERATION, "validateDuplicateEmail");
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATE_NICKNAME)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "nickname")
                    .addContext(ErrorContextKeys.OPERATION, "validateDuplicateNickname");
        }
    }
}
