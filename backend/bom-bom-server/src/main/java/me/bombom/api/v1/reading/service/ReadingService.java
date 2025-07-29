package me.bombom.api.v1.reading.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private static final int INITIAL_COUNT = 0;
    private static final int INITIAL_WEEKLY_GOAL_COUNT = 3;

    private final MemberRepository memberRepository;
    private final ContinueReadingRepository continueReadingRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final WeeklyReadingRepository weeklyReadingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeReadingInformation(Long memberId) {
        log.info("읽기 정보 초기화 시작 - memberId: {}", memberId);
        
        if (memberId == null) {
            log.error("memberId가 null입니다!");
            throw new IllegalArgumentException("memberId cannot be null");
        }
        
        try {
            log.info("ContinueReading 생성 시작 - memberId: {}", memberId);
            ContinueReading newContinueReading = ContinueReading.builder()
                    .memberId(memberId)
                    .dayCount(INITIAL_COUNT)
                    .build();
            log.info("ContinueReading 객체 생성 완료: {}", newContinueReading);
            
            ContinueReading savedContinueReading = continueReadingRepository.save(newContinueReading);
            log.info("ContinueReading 저장 완료 - id: {}, memberId: {}", 
                    savedContinueReading.getId(), savedContinueReading.getMemberId());

            log.info("TodayReading 생성 시작 - memberId: {}", memberId);
            TodayReading newTodayReading = TodayReading.builder()
                    .memberId(memberId)
                    .totalCount(INITIAL_COUNT)
                    .currentCount(INITIAL_COUNT)
                    .build();
            log.info("TodayReading 객체 생성 완료: {}", newTodayReading);
            
            TodayReading savedTodayReading = todayReadingRepository.save(newTodayReading);
            log.info("TodayReading 저장 완료 - id: {}, memberId: {}", 
                    savedTodayReading.getId(), savedTodayReading.getMemberId());

            log.info("WeeklyReading 생성 시작 - memberId: {}", memberId);
            WeeklyReading newWeeklyReading = WeeklyReading.builder()
                    .memberId(memberId)
                    .goalCount(INITIAL_WEEKLY_GOAL_COUNT)
                    .currentCount(INITIAL_COUNT)
                    .build();
            log.info("WeeklyReading 객체 생성 완료: {}", newWeeklyReading);
            
            WeeklyReading savedWeeklyReading = weeklyReadingRepository.save(newWeeklyReading);
            log.info("WeeklyReading 저장 완료 - id: {}, memberId: {}", 
                    savedWeeklyReading.getId(), savedWeeklyReading.getMemberId());
            
            log.info("모든 읽기 정보 초기화 완료 - memberId: {}", memberId);
            
        } catch (Exception e) {
            log.error("읽기 정보 초기화 중 예외 발생!");
            log.error("memberId: {}", memberId);
            log.error("에러 메시지: {}", e.getMessage());
            log.error("에러 타입: {}", e.getClass().getSimpleName());
            log.error("스택 트레이스:", e);
            throw e;
        }
    }

    @Transactional
    public WeeklyGoalCountResponse updateWeeklyGoalCount(UpdateWeeklyGoalCountRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyReading.updateGoalCount(request.weeklyGoalCount());
        return WeeklyGoalCountResponse.from(weeklyReading);
    }

    public ReadingInformationResponse getReadingInformation(Member member) {
        Long memberId = member.getId();
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return ReadingInformationResponse.of(continueReading, todayReading, weeklyReading);
    }

    @Transactional
    public void updateReadingCount(Article article){
        updateTodayReadingCount(article);
        updateWeeklyReadingCount(article);
    }

    private void updateTodayReadingCount(Article article) {
        if(article.isArrivedToday()) {
            TodayReading todayReading = todayReadingRepository.findByMemberId(article.getMemberId())
                    .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
            todayReading.increaseCurrentCount();
        }
    }

    private void updateWeeklyReadingCount(Article article) {
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(article.getMemberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyReading.increaseCurrentCount();
    }
}

