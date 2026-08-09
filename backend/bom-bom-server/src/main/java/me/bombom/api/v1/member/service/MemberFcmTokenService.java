package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.repository.MemberFcmTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFcmTokenService {

    private final MemberFcmTokenRepository memberFcmTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        memberFcmTokenRepository.bulkDeleteAllByMemberId(memberId);
    }
}
