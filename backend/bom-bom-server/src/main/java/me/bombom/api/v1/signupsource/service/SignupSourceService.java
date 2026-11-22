package me.bombom.api.v1.signupsource.service;

import java.time.LocalDateTime;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.signupsource.domain.SignupSource;
import me.bombom.api.v1.signupsource.dto.request.SaveSignupSourceRequest;
import me.bombom.api.v1.signupsource.dto.response.SignupSourceStatusResponse;
import me.bombom.api.v1.signupsource.repository.MemberSignupSourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SignupSourceService {

    private final MemberSignupSourceRepository memberSignupSourceRepository;
    private final LocalDateTime askSince;

    /**
     * askSince 이후에 가입한 회원에게만 가입 경로를 묻는다. 기능 배포 전에 가입한 회원은 묻지 않는다.
     */
    public SignupSourceService(
            MemberSignupSourceRepository memberSignupSourceRepository,
            @Value("${bombom.signup-source.ask-since:2026-11-24T00:00:00}") String askSince
    ) {
        this.memberSignupSourceRepository = memberSignupSourceRepository;
        this.askSince = LocalDateTime.parse(askSince);
    }

    public SignupSourceStatusResponse getStatus(Member member) {
        boolean isTarget = member.getCreatedAt() != null && !member.getCreatedAt().isBefore(askSince);
        boolean hasAnswered = memberSignupSourceRepository.existsByMemberId(member.getId());
        return new SignupSourceStatusResponse(isTarget && !hasAnswered);
    }

    @Transactional
    public void save(Member member, SaveSignupSourceRequest request) {
        String detail = normalizeDetail(request);
        memberSignupSourceRepository.insertIfAbsent(member.getId(), request.source().name(), detail);
    }

    /**
     * 탈퇴하면 자유 입력인 직접 입력 경로만 지운다. 가입 경로 값은 유입 분석을 위해 남긴다.
     */
    @Transactional
    public void clearDetailByMemberId(Long memberId) {
        memberSignupSourceRepository.clearDetailByMemberId(memberId);
    }

    private String normalizeDetail(SaveSignupSourceRequest request) {
        if (request.source() != SignupSource.ETC) {
            return null;
        }
        String detail = request.detail() == null ? "" : request.detail().strip();
        if (detail.isEmpty()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("signupSource", request.source())
                    .addContext("reason", "기타를 고르면 경로를 직접 입력해야 한다");
        }
        return detail;
    }
}
