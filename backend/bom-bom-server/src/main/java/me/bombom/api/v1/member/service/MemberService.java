package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.enums.SignupValidateField;
import me.bombom.api.v1.auth.enums.SignupValidateStatus;
import me.bombom.api.v1.auth.util.UserInfoValidator;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;
import me.bombom.api.v1.member.event.MemberSignupEvent;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.withdraw.event.WithdrawEvent;
import me.bombom.api.v1.withdraw.service.WithdrawService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final long MEMBER_ROLE_ID = 1L;

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final WithdrawService withdrawService;
    private final UserInfoValidator userInfoValidator;

    @Transactional
    public Member signup(PendingOAuth2Member pendingMember, MemberSignupRequest signupRequest) {
        userInfoValidator.validateNickname(signupRequest.nickname());
        userInfoValidator.validateEmail(signupRequest.email());

        Member newMember = Member.builder()
                .provider(pendingMember.getProvider())
                .providerId(pendingMember.getProviderId())
                .email(signupRequest.email().toLowerCase())
                .profileImageUrl(pendingMember.getProfileUrl())
                .nickname(signupRequest.nickname().strip())
                .gender(signupRequest.gender())
                .roleId(MEMBER_ROLE_ID)
                .birthDate(signupRequest.birthDate())
                .build();
        Member savedMember = memberRepository.save(newMember);
        applicationEventPublisher.publishEvent(new MemberSignupEvent(savedMember.getId()));
        return savedMember;
    }

    public SignupValidateStatus validateSignupField(SignupValidateField field, String value) {
        String normalized = value.strip().toLowerCase();
        return switch (field) {
            case NICKNAME -> validateSignupNickname(normalized);
            //이메일 전체가 옴
            case EMAIL -> validateSignupEmail(normalized);
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

        /*
        회원 탈퇴 신청 시 즉시 withdrawnMember로 정보 이전
        이벤트에서 회원 관련된 모든 정보 제거: articles, pet, highlight, bookmark, reading, subscribe
         */
        withdrawService.migrateDeletedMember(member);
        applicationEventPublisher.publishEvent(new WithdrawEvent(memberId));

        memberRepository.delete(member);
        log.info("회원 탈퇴 처리 완료. MemberId: {}", memberId);
    }

    private SignupValidateStatus validateSignupNickname(String value) {
        if (!userInfoValidator.isValidNicknameFormat(value)) {
            return SignupValidateStatus.INVALID_FORMAT;
        }
        if (userInfoValidator.isDuplicateNickname(value)) {
            return SignupValidateStatus.DUPLICATE;
        }
        return SignupValidateStatus.OK;
    }

    private SignupValidateStatus validateSignupEmail(String value) {
        if (!userInfoValidator.isValidEmailFormat(value)) {
            return SignupValidateStatus.INVALID_FORMAT;
        }
        if (userInfoValidator.isDuplicateEmail(value)) {
            return SignupValidateStatus.DUPLICATE;
        }
        return SignupValidateStatus.OK;
    }
}
