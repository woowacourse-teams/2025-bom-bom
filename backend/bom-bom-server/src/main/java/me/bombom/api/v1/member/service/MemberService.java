package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
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

    // TODO : 회원가입 입력 정보 양식 반영
    @Transactional
    public Member signup(PendingOAuth2Member pendingMember, MemberSignupRequest signupRequest) {
        Member newMember = Member.builder()
                .provider(pendingMember.getProvider())
                .providerId(pendingMember.getProviderId())
                .email(signupRequest.email())
                .profileImageUrl(pendingMember.getProfileUrl())
                .nickname(signupRequest.nickname())
                .gender(signupRequest.gender())
                .roleId(MEMBER_ROLE_ID)
                .build();
        Member savedMember = memberRepository.save(newMember);
        applicationEventPublisher.publishEvent(new MemberSignupEvent(savedMember.getId()));
        return savedMember;
    }

    public MemberProfileResponse getProfile(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, id)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "member")
                );
        return MemberProfileResponse.from(member);
    }
}
