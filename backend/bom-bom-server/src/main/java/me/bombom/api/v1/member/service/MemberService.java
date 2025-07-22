package me.bombom.api.v1.member.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // TODO : 회원가입 입력 정보 양식 반영
    public Member signup(PendingOAuth2Member pendingMember, MemberSignupRequest signupRequest) {
        validateDuplicateNickname(signupRequest);
        Member newMember = Member.builder()
                .provider(pendingMember.getProvider())
                .providerId(pendingMember.getProviderId())
                .email("tempEmail")
                .profileImageUrl(pendingMember.getProfileUrl())
                .nickname(signupRequest.nickname())
                .birthDate(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                .gender(signupRequest.gender())
                .roleId(0L)
                .build();
        return memberRepository.save(newMember);
    }

    private void validateDuplicateNickname(MemberSignupRequest signupRequest) {
        if (memberRepository.existsByNickname(signupRequest.nickname())) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATE_NICKNAME);
        }
    }
}
