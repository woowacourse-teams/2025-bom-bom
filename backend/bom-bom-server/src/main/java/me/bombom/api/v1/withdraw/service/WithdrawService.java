package me.bombom.api.v1.withdraw.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawService {

    private static final int EXPIRATION_DAYS = 90;

    private final MemberRepository memberRepository;
    private final WithdrawnMemberRepository withdrawnMemberRepository;
    private final HighlightRepository highlightRepository;
    private final ContinueReadingRepository continueReadingRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void migrateDeletedMember(Member member) {
        withdrawnMemberRepository.save(createWithdrawnMember(member));
        log.info("탈퇴한 회원 정보 이전 성공. memberId:{}", member.getId());
    }

    @Transactional
    public void deleteExpiredWithdrawnMembers() {
        withdrawnMemberRepository.deleteAllByExpireDate(LocalDate.now());
        log.info("만료된 회원 정보 삭제 성공");
    }

    private WithdrawnMember createWithdrawnMember(Member member) {
        return WithdrawnMember.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .birthDate(member.getBirthDate())
                .gender(member.getGender())
                .joinedDate(member.getCreatedAt().toLocalDate())
                .deletedDate(LocalDate.now())
                .expireDate(LocalDate.now().plusDays(EXPIRATION_DAYS))
                .continueReading(getContinueReading(member.getId()))
                .bookmarkedCount(getBookmarkedCount(member.getId()))
                .highlightCount(getHighlightCount(member.getId()))
                .build();
    }

    private int getContinueReading(Long memberId){
        Optional<ContinueReading> continueReading = continueReadingRepository.findByMemberId(memberId);
        if(continueReading.isEmpty()){
            log.warn("탈퇴한 회원의 연속 읽기 정보를 찾을 수 없습니다. memberId:{}", memberId);
            return 0;
        }
        return continueReading.get().getDayCount();
    }

    private int getBookmarkedCount(Long memberId){
        return (int) bookmarkRepository.countByMemberId(memberId);
    }

    private int getHighlightCount(Long memberId){
        return highlightRepository.countByMemberId(memberId);
    }
}
