package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;
import me.bombom.api.v1.member.event.MemberSignupEvent;
import me.bombom.api.v1.member.repository.MemberRepository;
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

    // TODO : 회원가입 입력 정보 양식 반영
    @Transactional
    public Member signup(PendingOAuth2Member pendingMember, MemberSignupRequest signupRequest) {
        log.info("회원가입 시작 - email: {}, nickname: {}", signupRequest.email(), signupRequest.nickname());
        
        Member newMember = Member.builder()
                .provider(pendingMember.getProvider())
                .providerId(pendingMember.getProviderId())
                .email(signupRequest.email())
                .profileImageUrl(pendingMember.getProfileUrl())
                .nickname(signupRequest.nickname())
                .gender(signupRequest.gender())
                .roleId(MEMBER_ROLE_ID)
                .build();
        
        log.info("회원 정보 생성 완료, DB 저장 시작");
        Member savedMember = memberRepository.save(newMember);
        log.info("회원 DB 저장 완료 - memberId: {}", savedMember.getId());
        
        log.info("회원가입 이벤트 발행 시작 - memberId: {}", savedMember.getId());
        try {
            MemberSignupEvent event = new MemberSignupEvent(savedMember.getId());
            log.info("MemberSignupEvent 객체 생성 완료 - memberId: {}", event.getMemberId());
            
            applicationEventPublisher.publishEvent(event);
            log.info("회원가입 이벤트 발행 완료 - memberId: {}", savedMember.getId());
        } catch (Exception e) {
            log.error("회원가입 이벤트 발행 중 오류 발생 - memberId: {}, error: {}", 
                    savedMember.getId(), e.getMessage(), e);
            throw e; // 이벤트 발행 실패시 회원가입도 실패시킴
        }
        
        log.info("회원가입 전체 프로세스 완료 - memberId: {}", savedMember.getId());
        return savedMember;
    }

    public MemberProfileResponse getProfile(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return MemberProfileResponse.from(member);
    }
}
