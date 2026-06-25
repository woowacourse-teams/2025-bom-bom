package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeFilter;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.EligibilityReason;
import me.bombom.api.v1.challenge.domain.RegistrationPhase;
import me.bombom.api.v1.challenge.dto.response.ChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeEligibilityResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeLandingResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeTeamListResponse;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupItemRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterGroupItemRepository newsletterGroupItemRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private MutableClock clock;

    @Test
    void 챌린지_목록이_없으면_빈_리스트를_반환한다() {
        // when
        List<?> result = challengeService.getChallenges(null, ChallengeFilter.DEFAULT);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 비회원_챌린지_목록은_참여정보를_미참여로_응답한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Challenge challenge = saveChallengeWithNewsletterOnly(
                "비회원목록챌린지",
                today.plusDays(1),
                today.plusDays(10),
                10
        );

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null, ChallengeFilter.DEFAULT);

        // then
        ChallengeResponse response = result.getFirst();
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(response.id()).isEqualTo(challenge.getId());
            softly.assertThat(response.participantCount()).isZero();
            softly.assertThat(response.newsletters()).hasSize(1);
            softly.assertThat(response.status()).isEqualTo(ChallengeStatus.BEFORE_START);
            softly.assertThat(response.participationInfo().isJoined()).isFalse();
        });
    }

    @Test
    void 참여중인_진행_챌린지_목록은_진행률과_참가자수를_응답한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("진행목록회원", "challenge-list-ongoing");
        Challenge challenge = saveChallengeWithNewsletter(
                "진행목록챌린지",
                today,
                today.plusDays(9),
                10,
                member
        );
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                5
        ));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member, ChallengeFilter.DEFAULT);

        // then
        ChallengeResponse response = result.getFirst();
        ChallengeDetailResponse detail = response.participationInfo();
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(response.participantCount()).isEqualTo(1L);
            softly.assertThat(response.status()).isEqualTo(ChallengeStatus.ONGOING);
            softly.assertThat(response.registrationPhase()).isEqualTo(RegistrationPhase.LATE);
            softly.assertThat(detail.isJoined()).isTrue();
            softly.assertThat(detail.progress()).isEqualTo(50);
            softly.assertThat(detail.grade()).isNull();
            softly.assertThat(detail.isSurvived()).isTrue();
        });
    }

    @Test
    void 참여중인_종료_챌린지_목록은_최종_등급을_응답한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 20);
        clock.setDate(today);
        Member member = saveMember("종료목록회원", "challenge-list-ended");
        Challenge challenge = saveChallengeWithNewsletter(
                "종료목록챌린지",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10,
                member
        );
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                10
        ));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member, ChallengeFilter.DEFAULT);

        // then
        ChallengeDetailResponse detail = result.getFirst().participationInfo();
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().status()).isEqualTo(ChallengeStatus.COMPLETED);
            softly.assertThat(detail.isJoined()).isTrue();
            softly.assertThat(detail.progress()).isEqualTo(100);
            softly.assertThat(detail.grade()).isEqualTo(ChallengeGrade.GOLD);
            softly.assertThat(detail.isSurvived()).isTrue();
        });
    }

    @Test
    void 요약_챌린지_목록은_참여중_지각모집_사전모집_순으로_정렬한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("요약목록회원", "challenge-summary");
        Challenge joined = saveChallengeWithNewsletter(
                "참여중챌린지",
                today.minusDays(1),
                today.plusDays(8),
                10,
                member
        );
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                joined.getId(),
                member.getId(),
                1
        ));
        Challenge late = saveChallengeWithNewsletterOnly("지각모집챌린지", today, today.plusDays(9), 10);
        Challenge early = saveChallengeWithNewsletterOnly("사전모집챌린지", today.plusDays(1), today.plusDays(10), 10);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member, ChallengeFilter.SUMMARY);

        // then
        assertThat(result)
                .extracting(ChallengeResponse::id)
                .containsExactly(joined.getId(), late.getId(), early.getId());
    }

    @Test
    void 토요일에_진행_중인_챌린지_조회_시_빈_리스트를_반환한다() {
        // given
        LocalDate saturday = LocalDate.of(2025, 3, 15);
        saveChallenge("진행 중 챌린지", saturday.minusDays(1), saturday.plusDays(1), 3);

        // when
        List<Challenge> result = challengeService.getOngoingChallenges(saturday);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 일요일에_진행_중인_챌린지_조회_시_빈_리스트를_반환한다() {
        // given
        LocalDate sunday = LocalDate.of(2025, 3, 16);
        saveChallenge("진행 중 챌린지", sunday.minusDays(1), sunday.plusDays(1), 3);

        // when
        List<Challenge> result = challengeService.getOngoingChallenges(sunday);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 평일에_진행_중인_챌린지를_조회한다() {
        // given
        LocalDate monday = LocalDate.of(2025, 3, 17);
        Challenge ongoing = saveChallenge("진행 중 챌린지", monday.minusDays(1), monday.plusDays(1), 3);
        saveChallenge("시작 전 챌린지", monday.plusDays(1), monday.plusDays(5), 5);
        saveChallenge("종료된 챌린지", monday.minusDays(5), monday.minusDays(1), 5);

        // when
        List<Challenge> result = challengeService.getOngoingChallenges(monday);

        // then
        assertThat(result)
                .extracting(Challenge::getId)
                .containsExactly(ongoing.getId());
    }

    @Test
    void 종료됐고_뱃지를_아직_발급하지_않은_장기_챌린지만_조회한다() {
        // given
        LocalDate today = LocalDate.of(2025, 3, 17);
        Challenge pending = saveChallenge("뱃지 발급 대상", today.minusDays(30), today.minusDays(1), 20);
        saveChallenge("짧은 챌린지", today.minusDays(10), today.minusDays(1), 10);
        saveChallenge("진행 중 챌린지", today.minusDays(1), today.plusDays(10), 20);

        Challenge issued = saveChallenge("이미 발급된 챌린지", today.minusDays(30), today.minusDays(1), 20);
        issued.markBadgeAsIssued();
        challengeRepository.save(issued);

        // when
        List<Challenge> result = challengeService.getEndedChallengesPendingBadge(today);

        // then
        assertThat(result)
                .extracting(Challenge::getId)
                .containsExactly(pending.getId());
    }

    @Test
    void 챌린지_상세_정보를_조회한다() {
        // given
        Challenge challenge = saveChallenge(
                "상세챌린지",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );

        // when
        ChallengeInfoResponse result = challengeService.getChallengeInfo(challenge.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.name()).isEqualTo("상세챌린지");
            softly.assertThat(result.totalDays()).isEqualTo(10);
            softly.assertThat(result.requiredDays()).isEqualTo(8);
        });
    }

    @Test
    void 존재하지_않는_챌린지_상세_정보는_조회할_수_없다() {
        assertThatThrownBy(() -> challengeService.getChallengeInfo(-1L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 챌린지_랜딩_정보는_뱃지_발급_대상과_뉴스레터를_함께_응답한다() {
        // given
        Challenge challenge = saveChallengeWithNewsletterOnly(
                "랜딩챌린지",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 25),
                15
        );

        // when
        ChallengeLandingResponse result = challengeService.getChallengeLanding(challenge.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.name()).isEqualTo("랜딩챌린지");
            softly.assertThat(result.grantsBadge()).isTrue();
            softly.assertThat(result.newsletters()).hasSize(1);
        });
    }

    @Test
    void 존재하지_않는_챌린지_랜딩_정보는_조회할_수_없다() {
        assertThatThrownBy(() -> challengeService.getChallengeLanding(-1L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 로그인하지_않은_회원은_챌린지_참여_불가_사유를_반환한다() {
        // given
        Challenge challenge = saveChallenge(
                "로그인필요챌린지",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );

        // when
        ChallengeEligibilityResponse result = challengeService.checkEligibility(challenge.getId(), null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.canApply()).isFalse();
            softly.assertThat(result.reason()).isEqualTo(EligibilityReason.NOT_LOGGED_IN);
        });
    }

    @Test
    void 구독한_회원은_챌린지_신청_가능_사유를_응답한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("신청가능회원", "challenge-eligible");
        Challenge challenge = saveChallengeWithNewsletter(
                "신청가능챌린지",
                today.plusDays(1),
                today.plusDays(10),
                10,
                member
        );

        // when
        ChallengeEligibilityResponse result = challengeService.checkEligibility(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.canApply()).isTrue();
            softly.assertThat(result.reason()).isEqualTo(EligibilityReason.ELIGIBLE);
        });
    }

    @Test
    void 이미_신청한_챌린지는_중복_신청해도_참가자를_추가하지_않는다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("중복신청회원", "challenge-duplicate");
        Challenge challenge = saveChallengeWithNewsletter(
                "중복신청챌린지",
                today.plusDays(1),
                today.plusDays(10),
                10,
                member
        );
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                0
        ));

        // when
        challengeService.applyChallenge(challenge.getId(), member);

        // then
        assertThat(challengeParticipantRepository.findAllByChallengeId(challenge.getId())).hasSize(1);
    }

    @Test
    void 시작_전_챌린지_신청은_팀_없이_참가자를_생성한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("사전신청회원", "challenge-early-apply");
        Challenge challenge = saveChallengeWithNewsletter(
                "사전신청챌린지",
                today.plusDays(1),
                today.plusDays(10),
                10,
                member
        );

        // when
        challengeService.applyChallenge(challenge.getId(), member);

        // then
        ChallengeParticipant participant = challengeParticipantRepository
                .findByChallengeIdAndMemberId(challenge.getId(), member.getId())
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(participant.getChallengeTeamId()).isNull();
            softly.assertThat(participant.isSurvived()).isTrue();
        });
    }

    @Test
    void 구독한_뉴스레터가_없으면_챌린지를_신청할_수_없다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("미구독회원", "challenge-not-subscribed");
        Challenge challenge = saveChallenge(
                "미구독챌린지",
                today.plusDays(1),
                today.plusDays(10),
                10
        );

        // when & then
        assertThatThrownBy(() -> challengeService.applyChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.PRECONDITION_FAILED);
    }

    @Test
    void 모집이_마감된_챌린지는_신청할_수_없다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 8);
        clock.setDate(today);
        Member member = saveMember("마감신청회원", "challenge-closed");
        Challenge challenge = saveChallenge(
                "마감챌린지",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );

        // when & then
        assertThatThrownBy(() -> challengeService.applyChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void 진행중_챌린지_신청은_가장_인원이_적은_팀에_배정한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member applicant = saveMember("팀배정회원", "challenge-team-applicant");
        Member existing = saveMember("기존팀회원", "challenge-team-existing");
        Challenge challenge = saveChallengeWithNewsletter(
                "팀배정챌린지",
                today,
                today.plusDays(10),
                10,
                applicant
        );
        ChallengeTeam firstTeam = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
        ChallengeTeam secondTeam = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipantWithTeam(
                challenge.getId(),
                existing.getId(),
                firstTeam.getId(),
                0,
                0
        ));

        // when
        challengeService.applyChallenge(challenge.getId(), applicant);

        // then
        ChallengeParticipant participant = challengeParticipantRepository
                .findByChallengeIdAndMemberId(challenge.getId(), applicant.getId())
                .orElseThrow();
        assertThat(participant.getChallengeTeamId()).isEqualTo(secondTeam.getId());
    }

    @Test
    void 시작_전_챌린지는_신청을_취소할_수_있다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("취소회원", "challenge-cancel");
        Challenge challenge = saveChallenge(
                "취소챌린지",
                today.plusDays(1),
                today.plusDays(10),
                10
        );
        challengeParticipantRepository.save(TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                0
        ));

        // when
        challengeService.cancelChallenge(challenge.getId(), member);

        // then
        assertThat(challengeParticipantRepository.findByChallengeIdAndMemberId(challenge.getId(), member.getId()))
                .isEmpty();
    }

    @Test
    void 신청하지_않은_시작전_챌린지는_취소할_수_없다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("미신청취소회원", "challenge-cancel-missing");
        Challenge challenge = saveChallenge(
                "미신청취소챌린지",
                today.plusDays(1),
                today.plusDays(10),
                10
        );

        // when & then
        assertThatThrownBy(() -> challengeService.cancelChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 시작된_챌린지는_신청을_취소할_수_없다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 5);
        clock.setDate(today);
        Member member = saveMember("취소불가회원", "challenge-cancel-started");
        Challenge challenge = saveChallenge(
                "취소불가챌린지",
                today,
                today.plusDays(10),
                10
        );

        // when & then
        assertThatThrownBy(() -> challengeService.cancelChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void 팀_목록은_내_팀과_팀_번호를_함께_응답한다() {
        // given
        Member member = saveMember("팀조회회원", "challenge-team-list-success");
        Challenge challenge = saveChallenge(
                "팀조회챌린지",
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 16),
                10
        );
        ChallengeTeam firstTeam = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
        ChallengeTeam secondTeam = challengeTeamRepository.save(TestFixture.createChallengeTeam(challenge.getId(), 0));
        challengeParticipantRepository.save(TestFixture.createChallengeParticipantWithTeam(
                challenge.getId(),
                member.getId(),
                secondTeam.getId(),
                0,
                0
        ));

        // when
        ChallengeTeamListResponse result = challengeService.getTeamList(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalTeamCount()).isEqualTo(2);
            softly.assertThat(result.myTeamId()).isEqualTo(secondTeam.getId());
            softly.assertThat(result.teams().get(0).teamId()).isEqualTo(firstTeam.getId());
            softly.assertThat(result.teams().get(0).teamNumber()).isEqualTo(1);
            softly.assertThat(result.teams().get(0).isMyTeam()).isFalse();
            softly.assertThat(result.teams().get(1).teamId()).isEqualTo(secondTeam.getId());
            softly.assertThat(result.teams().get(1).teamNumber()).isEqualTo(2);
            softly.assertThat(result.teams().get(1).isMyTeam()).isTrue();
        });
    }

    @Test
    void 존재하지_않는_챌린지는_팀_목록을_조회할_수_없다() {
        // given
        Member member = saveMember("팀목록회원", "challenge-team-list");

        // when & then
        assertThatThrownBy(() -> challengeService.getTeamList(-1L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    private Challenge saveChallenge(String name, LocalDate startDate, LocalDate endDate, int totalDays) {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup(name + " 그룹"));
        return challengeRepository.save(TestFixture.createChallenge(
                name,
                startDate,
                endDate,
                totalDays,
                group.getId()
        ));
    }

    private Challenge saveChallengeWithNewsletterOnly(
            String name,
            LocalDate startDate,
            LocalDate endDate,
            int totalDays
    ) {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup(name + " 그룹"));
        Newsletter newsletter = saveNewsletter(name + " 뉴스레터");
        newsletterGroupItemRepository.save(TestFixture.createNewsletterGroupItem(group.getId(), newsletter.getId()));
        return challengeRepository.save(TestFixture.createChallenge(
                name,
                startDate,
                endDate,
                totalDays,
                group.getId()
        ));
    }

    private Challenge saveChallengeWithNewsletter(
            String name,
            LocalDate startDate,
            LocalDate endDate,
            int totalDays,
            Member subscribedMember
    ) {
        NewsletterGroup group = newsletterGroupRepository.save(TestFixture.createNewsletterGroup(name + " 그룹"));
        Newsletter newsletter = saveNewsletter(name + " 뉴스레터");
        newsletterGroupItemRepository.save(TestFixture.createNewsletterGroupItem(group.getId(), newsletter.getId()));
        subscribeRepository.save(Subscribe.builder()
                .memberId(subscribedMember.getId())
                .newsletterId(newsletter.getId())
                .build());
        return challengeRepository.save(TestFixture.createChallenge(
                name,
                startDate,
                endDate,
                totalDays,
                group.getId()
        ));
    }

    private Newsletter saveNewsletter(String name) {
        Category category = categoryRepository.save(Category.builder()
                .name(name + "카테고리")
                .build());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(false));
        return newsletterRepository.save(TestFixture.createNewsletter(
                name,
                name + "@bombom.news",
                category.getId(),
                detail.getId()
        ));
    }

    private Member saveMember(String nickname, String providerId) {
        return memberRepository.save(TestFixture.createUniqueMember(nickname, providerId));
    }
}
