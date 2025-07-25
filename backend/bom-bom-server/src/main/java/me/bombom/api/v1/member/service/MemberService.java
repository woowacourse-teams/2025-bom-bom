package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.dto.response.MemberProfileResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final ReadingService readingService;
    private final MemberRepository memberRepository;

    // TODO : 회원가입 입력 정보 양식 반영
    public Member signup(PendingOAuth2Member pendingMember, MemberSignupRequest signupRequest) {
        Member newMember = Member.builder()
                .provider(pendingMember.getProvider())
                .providerId(pendingMember.getProviderId())
                .email(signupRequest.email())
                .profileImageUrl(pendingMember.getProfileUrl())
                .nickname(signupRequest.nickname())
                .gender(signupRequest.gender())
                .roleId(1L)
                .build();
        Member savedMember = memberRepository.save(newMember);
        readingService.initializeReadingInformation(savedMember.getId());
        return savedMember;
    }

    public MemberProfileResponse getProfile(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return MemberProfileResponse.from(member);
    }
}
