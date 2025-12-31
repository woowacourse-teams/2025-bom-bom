package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeNewsletter;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeStatus;
import me.bombom.api.v1.challenge.domain.EligibilityReason;
import me.bombom.api.v1.challenge.dto.response.ChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeEligibilityResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeResponse;
import me.bombom.api.v1.challenge.repository.ChallengeNewsletterRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeNewsletterRepository challengeNewsletterRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    private Member member;
    private List<Category> categories;
    private List<Newsletter> newsletters;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        challengeParticipantRepository.deleteAllInBatch();
        challengeNewsletterRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        subscribeRepository.deleteAllInBatch();
        newsletterRepository.deleteAllInBatch();
        newsletterDetailRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);

        List<NewsletterDetail> newsletterDetails = TestFixture.createNewsletterDetails();
        newsletterDetailRepository.saveAll(newsletterDetails);

        newsletters = TestFixture.createNewslettersWithDetails(categories, newsletterDetails);
        newsletterRepository.saveAll(newsletters);

        today = LocalDate.now();
    }

    @Test
    void 비로그인_상태로_챌린지_목록_조회() {
        // given
        Challenge challenge1 = TestFixture.createChallenge("첫 번째 챌린지", 1, today.minusDays(10), today.plusDays(10));
        Challenge challenge2 = TestFixture.createChallenge("두 번째 챌린지", 2, today.plusDays(5), today.plusDays(15));
        challengeRepository.saveAll(List.of(challenge1, challenge2));

        ChallengeNewsletter challengeNewsletter1 = TestFixture.createChallengeNewsletter(challenge1.getId(), newsletters.get(0).getId());
        ChallengeNewsletter challengeNewsletter2 = TestFixture.createChallengeNewsletter(challenge2.getId(), newsletters.get(1).getId());
        challengeNewsletterRepository.saveAll(List.of(challengeNewsletter1, challengeNewsletter2));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result)
                    .extracting("id")
                    .containsExactlyInAnyOrder(challenge1.getId(), challenge2.getId());
            softly.assertThat(result)
                    .extracting("title")
                    .containsExactlyInAnyOrder("첫 번째 챌린지", "두 번째 챌린지");
            softly.assertThat(result)
                    .extracting(ChallengeResponse::detail)
                    .allMatch(detail -> !detail.isJoined());
            softly.assertThat(result)
                    .extracting(ChallengeResponse::detail)
                    .extracting(ChallengeDetailResponse::progress)
                    .containsOnly(0);
        });
    }

    @Test
    void 로그인_상태로_챌린지_목록_조회() {
        // given
        Challenge challenge1 = TestFixture.createChallenge("첫 번째 챌린지", 1, today.minusDays(10), today.plusDays(10));
        Challenge challenge2 = TestFixture.createChallenge("두 번째 챌린지", 2, today.plusDays(5), today.plusDays(15));
        challengeRepository.saveAll(List.of(challenge1, challenge2));

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(
                challenge1.getId(),
                member.getId(),
                5,
                true
        );
        challengeParticipantRepository.save(participant);

        ChallengeNewsletter challengeNewsletter1 = TestFixture.createChallengeNewsletter(challenge1.getId(), newsletters.get(0).getId());
        ChallengeNewsletter challengeNewsletter2 = TestFixture.createChallengeNewsletter(challenge2.getId(), newsletters.get(1).getId());
        challengeNewsletterRepository.saveAll(List.of(challengeNewsletter1, challengeNewsletter2));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            ChallengeResponse challenge1Response = result.stream()
                    .filter(r -> r.id().equals(challenge1.getId()))
                    .findFirst()
                    .orElseThrow();
            ChallengeResponse challenge2Response = result.stream()
                    .filter(r -> r.id().equals(challenge2.getId()))
                    .findFirst()
                    .orElseThrow();

            softly.assertThat(challenge1Response.detail().isJoined()).isTrue();
            softly.assertThat(challenge1Response.detail().progress()).isGreaterThan(0);
            softly.assertThat(challenge2Response.detail().isJoined()).isFalse();
            softly.assertThat(challenge2Response.detail().progress()).isEqualTo(0);
        });
    }

    @Test
    void 챌린지가_없을_때_빈_리스트_반환() {
        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 참가자_수_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(10), today.plusDays(10));
        challengeRepository.save(challenge);

        Member member1 = TestFixture.createUniqueMember("member1", "provider1");
        Member member2 = TestFixture.createUniqueMember("member2", "provider2");
        memberRepository.saveAll(List.of(member1, member2));

        ChallengeParticipant participant1 = TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 5, true);
        ChallengeParticipant participant2 = TestFixture.createChallengeParticipant(challenge.getId(), member2.getId(), 3, true);
        challengeParticipantRepository.saveAll(List.of(participant1, participant2));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).participantCount()).isEqualTo(2);
        });
    }

    @Test
    void 챌린지별_뉴스레터_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(10), today.plusDays(10));
        challengeRepository.save(challenge);

        ChallengeNewsletter challengeNewsletter1 = TestFixture.createChallengeNewsletter(challenge.getId(), newsletters.get(0).getId());
        ChallengeNewsletter challengeNewsletter2 = TestFixture.createChallengeNewsletter(challenge.getId(), newsletters.get(1).getId());
        challengeNewsletterRepository.saveAll(List.of(challengeNewsletter1, challengeNewsletter2));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).newsletters()).hasSize(2);
            softly.assertThat(result.get(0).newsletters())
                    .extracting("id")
                    .containsExactlyInAnyOrder(newsletters.get(0).getId(), newsletters.get(1).getId());
        });
    }

    @Test
    void 진행_중인_챌린지_상태_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("진행 중 챌린지", 1, today.minusDays(5), today.plusDays(5));
        challengeRepository.save(challenge);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).status()).isEqualTo(ChallengeStatus.ONGOING);
        });
    }

    @Test
    void 종료된_챌린지_상태_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("종료된 챌린지", 1, today.minusDays(20), today.minusDays(1));
        challengeRepository.save(challenge);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).status()).isEqualTo(ChallengeStatus.COMPLETED);
        });
    }

    @Test
    void 시작_전_챌린지_상태_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("시작 전 챌린지", 1, today.plusDays(5), today.plusDays(15));
        challengeRepository.save(challenge);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).status()).isEqualTo(ChallengeStatus.BEFORE_START);
        });
    }

    @Test
    void 종료된_챌린지_참여_결과_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("종료된 챌린지", 1, today.minusDays(20), today.minusDays(1));
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                18,
                true
        );
        challengeParticipantRepository.save(participant);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member);

        // then
        ChallengeDetailResponse detail = result.get(0).detail();
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(detail.isJoined()).isTrue();
            softly.assertThat(detail.progress()).isGreaterThan(0);
            softly.assertThat(detail.grade()).isNotNull();
            softly.assertThat(detail.isSuccess()).isNotNull();
        });
    }

    @Test
    void 참가하지_않은_챌린지_detail_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(10), today.plusDays(10));
        challengeRepository.save(challenge);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member);

        // then
        ChallengeDetailResponse detail = result.get(0).detail();
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(detail).isNotNull();
            softly.assertThat(detail.isJoined()).isFalse();
            softly.assertThat(detail.progress()).isEqualTo(0);
        });
    }

    @Test
    void 챌린지_상세_정보를_조회할_수_있다() {
        // given
        Challenge challenge = Challenge.builder()
                .name("챌린지1")
                .generation(1)
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 2, 4))
                .totalDays(31)
                .build();
        challengeRepository.save(challenge);

        // when
        ChallengeInfoResponse response = challengeService.getChallengeInfo(challenge.getId());

        // then
        assertSoftly(softly -> {
            assertThat(response.name()).isEqualTo("챌린지1");
            assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 1, 5));
            assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 2, 4));
            assertThat(response.generation()).isEqualTo(1);
        });
    }

    @Test
    void 존재하지_않는_챌린지_ID로_조회_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> challengeService.getChallengeInfo(0L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 비로그인_상태에서_챌린지_신청_가능_여부_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15));
        challengeRepository.save(challenge);

        // when
        ChallengeEligibilityResponse response = challengeService.checkEligibility(challenge.getId(), null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.canApply()).isFalse();
            softly.assertThat(response.reason()).isEqualTo(EligibilityReason.NOT_LOGGED_IN);
        });
    }

    @Test
    void 이미_시작된_챌린지의_신청_가능_여부_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(5), today.plusDays(10));
        challengeRepository.save(challenge);

        ChallengeNewsletter challengeNewsletter = TestFixture.createChallengeNewsletter(challenge.getId(), newsletters.get(0).getId());
        challengeNewsletterRepository.save(challengeNewsletter);

        Subscribe subscribe = TestFixture.createSubscribe(newsletters.get(0), member);
        subscribeRepository.save(subscribe);

        // when
        ChallengeEligibilityResponse response = challengeService.checkEligibility(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.canApply()).isFalse();
            softly.assertThat(response.reason()).isEqualTo(EligibilityReason.ALREADY_STARTED);
        });
    }

    @Test
    void 이미_신청한_챌린지의_신청_가능_여부_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15));
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0, true);
        challengeParticipantRepository.save(participant);

        ChallengeNewsletter challengeNewsletter = TestFixture.createChallengeNewsletter(challenge.getId(), newsletters.get(0).getId());
        challengeNewsletterRepository.save(challengeNewsletter);

        Subscribe subscribe = TestFixture.createSubscribe(newsletters.get(0), member);
        subscribeRepository.save(subscribe);

        // when
        ChallengeEligibilityResponse response = challengeService.checkEligibility(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.canApply()).isFalse();
            softly.assertThat(response.reason()).isEqualTo(EligibilityReason.ALREADY_APPLIED);
        });
    }

    @Test
    void 구독하지_않은_뉴스레터를_가진_챌린지의_신청_가능_여부_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15));
        challengeRepository.save(challenge);

        ChallengeNewsletter challengeNewsletter = TestFixture.createChallengeNewsletter(challenge.getId(), newsletters.get(0).getId());
        challengeNewsletterRepository.save(challengeNewsletter);

        // when
        ChallengeEligibilityResponse response = challengeService.checkEligibility(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.canApply()).isFalse();
            softly.assertThat(response.reason()).isEqualTo(EligibilityReason.NOT_SUBSCRIBED);
        });
    }

    @Test
    void 모든_조건을_만족하는_챌린지의_신청_가능_여부_조회() {
        // given
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15));
        challengeRepository.save(challenge);

        ChallengeNewsletter challengeNewsletter = TestFixture.createChallengeNewsletter(challenge.getId(), newsletters.get(0).getId());
        challengeNewsletterRepository.save(challengeNewsletter);

        Subscribe subscribe = TestFixture.createSubscribe(newsletters.get(0), member);
        subscribeRepository.save(subscribe);

        // when
        ChallengeEligibilityResponse response = challengeService.checkEligibility(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.canApply()).isTrue();
            softly.assertThat(response.reason()).isEqualTo(EligibilityReason.ELIGIBLE);
        });
    }

    @Test
    void 존재하지_않는_챌린지_ID로_신청_가능_여부_조회_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> challengeService.checkEligibility(0L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }
}
