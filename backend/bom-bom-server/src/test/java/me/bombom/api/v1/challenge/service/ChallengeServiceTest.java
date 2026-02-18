package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeStatus;
import me.bombom.api.v1.challenge.domain.EligibilityReason;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.response.ChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeEligibilityResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeInfoResponse;
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
import me.bombom.api.v1.newsletter.domain.NewsletterGroupItem;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupItemRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
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
    private NewsletterGroupRepository newsletterGroupRepository;

    @Autowired
    private NewsletterGroupItemRepository newsletterGroupItemRepository;

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

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    private Member member;
    private List<Category> categories;
    private List<Newsletter> newsletters;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        challengeParticipantRepository.deleteAllInBatch();
        challengeTeamRepository.deleteAllInBatch();
        newsletterGroupItemRepository.deleteAllInBatch();
        newsletterGroupRepository.deleteAllInBatch();
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
        NewsletterGroup group1 = TestFixture.createNewsletterGroup("그룹1");
        NewsletterGroup group2 = TestFixture.createNewsletterGroup("그룹2");
        newsletterGroupRepository.saveAll(List.of(group1, group2));

        Challenge challenge1 = TestFixture.createChallenge("첫 번째 챌린지", 1, today.minusDays(10), today.plusDays(10), group1.getId());
        Challenge challenge2 = TestFixture.createChallenge("두 번째 챌린지", 2, today.plusDays(5), today.plusDays(15), group2.getId());
        challengeRepository.saveAll(List.of(challenge1, challenge2));

        NewsletterGroupItem item1 = TestFixture.createNewsletterGroupItem(challenge1.getNewsletterGroupId(), newsletters.get(0).getId());
        NewsletterGroupItem item2 = TestFixture.createNewsletterGroupItem(challenge2.getNewsletterGroupId(), newsletters.get(1).getId());
        newsletterGroupItemRepository.saveAll(List.of(item1, item2));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then - 시작전 챌린지만 반환되어야 함 (challenge2만)
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).id()).isEqualTo(challenge2.getId());
            softly.assertThat(result.get(0).detail().isJoined()).isFalse();
            softly.assertThat(result.get(0).detail().progress()).isEqualTo(0);
        });
    }

    @Test
    void 로그인_상태로_챌린지_목록_조회() {
        // given
        NewsletterGroup group1 = TestFixture.createNewsletterGroup("그룹1");
        NewsletterGroup group2 = TestFixture.createNewsletterGroup("그룹2");
        newsletterGroupRepository.saveAll(List.of(group1, group2));

        Challenge challenge1 = TestFixture.createChallenge("첫 번째 챌린지", 1, today.minusDays(10), today.plusDays(10), group1.getId());
        Challenge challenge2 = TestFixture.createChallenge("두 번째 챌린지", 2, today.plusDays(5), today.plusDays(15), group2.getId());
        challengeRepository.saveAll(List.of(challenge1, challenge2));

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(
                challenge1.getId(),
                member.getId(),
                5,
                true
        );
        challengeParticipantRepository.save(participant);

        NewsletterGroupItem item1 = TestFixture.createNewsletterGroupItem(challenge1.getNewsletterGroupId(), newsletters.get(0).getId());
        NewsletterGroupItem item2 = TestFixture.createNewsletterGroupItem(challenge2.getNewsletterGroupId(), newsletters.get(1).getId());
        newsletterGroupItemRepository.saveAll(List.of(item1, item2));

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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        Member member1 = TestFixture.createUniqueMember("member1", "provider1");
        Member member2 = TestFixture.createUniqueMember("member2", "provider2");
        memberRepository.saveAll(List.of(member1, member2));

        ChallengeParticipant participant1 = TestFixture.createChallengeParticipant(challenge.getId(), member1.getId(), 5, true);
        ChallengeParticipant participant2 = TestFixture.createChallengeParticipant(challenge.getId(), member2.getId(), 3, true);
        challengeParticipantRepository.saveAll(List.of(participant1, participant2));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then - 시작전 챌린지이므로 반환되어야 함
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).participantCount()).isEqualTo(2);
        });
    }

    @Test
    void 챌린지별_뉴스레터_조회() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        NewsletterGroupItem item1 = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        NewsletterGroupItem item2 = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(1).getId());
        newsletterGroupItemRepository.saveAll(List.of(item1, item2));

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(null);

        // then - 시작전 챌린지이므로 반환되어야 함
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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("진행 중 챌린지", 1, today.minusDays(5), today.plusDays(5), group.getId());
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                3,
                true
        );
        challengeParticipantRepository.save(participant);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member);

        // then - 참여한 진행중 챌린지이므로 반환되어야 함
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).status()).isEqualTo(ChallengeStatus.ONGOING);
        });
    }

    @Test
    void 종료된_챌린지_상태_조회() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("종료된 챌린지", 1, today.minusDays(20), today.minusDays(1), group.getId());
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                15,
                true
        );
        challengeParticipantRepository.save(participant);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member);

        // then - 참여한 종료된 챌린지이므로 반환되어야 함
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).status()).isEqualTo(ChallengeStatus.COMPLETED);
        });
    }

    @Test
    void 시작_전_챌린지_상태_조회() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("시작 전 챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("종료된 챌린지", 1, today.minusDays(20), today.minusDays(1), group.getId());
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
            softly.assertThat(detail.isSurvived()).isNotNull();
        });
    }

    @Test
    void 참가하지_않은_챌린지_detail_조회() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        // when
        List<ChallengeResponse> result = challengeService.getChallenges(member);

        // then - 시작전 챌린지이므로 반환되어야 함
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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);

        Challenge challenge = Challenge.builder()
                .name("챌린지1")
                .generation(1)
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 2, 4))
                .totalDays(31)
                .newsletterGroupId(group.getId())
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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(5), today.plusDays(10), group.getId());
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0, true);
        challengeParticipantRepository.save(participant);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

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
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

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
    void 추가_신청_기간_내_시작된_챌린지_신청_가능_여부_조회() {
        // given: 시작일이 오늘, totalDays 21일 → 허용 영업일 3일. 오늘은 1영업일차 이내이므로 신청 가능
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge(
                "챌린지",
                today,
                today.plusDays(30),
                21,
                group.getId()
        );
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

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
    void 추가_신청_기간_지난_챌린지_신청_가능_여부_조회() {
        // given: 시작일이 10일 전, totalDays 21일 → 허용 영업일 3일. 이미 지나서 신청 불가
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge(
                "챌린지",
                today.minusDays(10),
                today.plusDays(20),
                21,
                group.getId()
        );
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

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
    void 존재하지_않는_챌린지_ID로_신청_가능_여부_조회_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> challengeService.checkEligibility(0L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 모든_조건을_만족하는_챌린지_신청() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

        Subscribe subscribe = TestFixture.createSubscribe(newsletters.get(0), member);
        subscribeRepository.save(subscribe);

        // when
        challengeService.applyChallenge(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(challengeParticipantRepository.existsByChallengeIdAndMemberId(challenge.getId(), member.getId())).isTrue();
        });
    }

    @Test
    void 존재하지_않는_챌린지_ID로_신청_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> challengeService.applyChallenge(0L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 이미_시작된_챌린지_신청_시_예외가_발생한다() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(5), today.plusDays(10), group.getId());
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

        Subscribe subscribe = TestFixture.createSubscribe(newsletters.get(0), member);
        subscribeRepository.save(subscribe);

        // when & then
        assertThatThrownBy(() -> challengeService.applyChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    void 이미_신청한_챌린지_신청_시_정상_반환한다() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0, true);
        challengeParticipantRepository.save(participant);
        long countBefore = challengeParticipantRepository.count();

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

        Subscribe subscribe = TestFixture.createSubscribe(newsletters.get(0), member);
        subscribeRepository.save(subscribe);

        // when
        challengeService.applyChallenge(challenge.getId(), member);
        long countAfter = challengeParticipantRepository.count();

        // then
        assertThat(countBefore).isEqualTo(1);
        assertThat(countAfter).isEqualTo(1); // 중복 신청되어도 개수 변경 없음
    }

    @Test
    void 구독하지_않은_뉴스레터를_가진_챌린지_신청_시_예외가_발생한다() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        NewsletterGroupItem item = TestFixture.createNewsletterGroupItem(challenge.getNewsletterGroupId(), newsletters.get(0).getId());
        newsletterGroupItemRepository.save(item);

        // when & then
        assertThatThrownBy(() -> challengeService.applyChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.PRECONDITION_FAILED.getMessage());
    }

    @Test
    void 신청한_챌린지_취소() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 0, true);
        challengeParticipantRepository.save(participant);

        // when
        challengeService.cancelChallenge(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(challengeParticipantRepository.existsByChallengeIdAndMemberId(challenge.getId(), member.getId())).isFalse();
        });
    }

    @Test
    void 존재하지_않는_챌린지_ID로_취소_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> challengeService.cancelChallenge(0L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 이미_시작된_챌린지_취소_시_예외가_발생한다() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.minusDays(5), today.plusDays(10), group.getId());
        challengeRepository.save(challenge);

        ChallengeParticipant participant = TestFixture.createChallengeParticipant(challenge.getId(), member.getId(), 5, true);
        challengeParticipantRepository.save(participant);

        // when & then
        assertThatThrownBy(() -> challengeService.cancelChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    void 신청하지_않은_챌린지_취소_시_예외가_발생한다() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        // when & then
        assertThatThrownBy(() -> challengeService.cancelChallenge(challenge.getId(), member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 팀_목록_조회_시_내_팀_포함() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        ChallengeTeam team1 = TestFixture.createChallengeTeam(challenge.getId(), 50);
        ChallengeTeam team2 = TestFixture.createChallengeTeam(challenge.getId(), 60);
        ChallengeTeam team3 = TestFixture.createChallengeTeam(challenge.getId(), 70);
        challengeTeamRepository.saveAll(List.of(team1, team2, team3));

        ChallengeParticipant participant = TestFixture.createChallengeParticipantWithTeam(
                challenge.getId(),
                member.getId(),
                team2.getId(),  // team2에 속함
                5,
                0
        );
        challengeParticipantRepository.save(participant);

        // when
        ChallengeTeamListResponse result = challengeService.getTeamList(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalTeamCount()).isEqualTo(3);
            softly.assertThat(result.myTeamId()).isEqualTo(team2.getId());
            softly.assertThat(result.teams()).hasSize(3);
            softly.assertThat(result.teams().get(0).teamId()).isEqualTo(team1.getId());
            softly.assertThat(result.teams().get(0).teamNumber()).isEqualTo(1);
            softly.assertThat(result.teams().get(0).isMyTeam()).isFalse();
            softly.assertThat(result.teams().get(1).teamId()).isEqualTo(team2.getId());
            softly.assertThat(result.teams().get(1).teamNumber()).isEqualTo(2);
            softly.assertThat(result.teams().get(1).isMyTeam()).isTrue();
            softly.assertThat(result.teams().get(2).teamId()).isEqualTo(team3.getId());
            softly.assertThat(result.teams().get(2).teamNumber()).isEqualTo(3);
            softly.assertThat(result.teams().get(2).isMyTeam()).isFalse();
        });
    }

    @Test
    void 팀_목록_조회_시_내_팀이_null인_경우() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        ChallengeTeam team1 = TestFixture.createChallengeTeam(challenge.getId(), 50);
        ChallengeTeam team2 = TestFixture.createChallengeTeam(challenge.getId(), 60);
        challengeTeamRepository.saveAll(List.of(team1, team2));

        // 참가는 했지만 팀에 배정되지 않은 경우
        ChallengeParticipant participant = TestFixture.createChallengeParticipant(
                challenge.getId(),
                member.getId(),
                5
        );
        challengeParticipantRepository.save(participant);

        // when
        ChallengeTeamListResponse result = challengeService.getTeamList(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalTeamCount()).isEqualTo(2);
            softly.assertThat(result.myTeamId()).isNull();
            softly.assertThat(result.teams()).hasSize(2);
            softly.assertThat(result.teams().get(0).isMyTeam()).isFalse();
            softly.assertThat(result.teams().get(1).isMyTeam()).isFalse();
        });
    }

    @Test
    void 팀_목록_조회_시_팀이_없는_경우() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        // when
        ChallengeTeamListResponse result = challengeService.getTeamList(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.totalTeamCount()).isEqualTo(0);
            softly.assertThat(result.myTeamId()).isNull();
            softly.assertThat(result.teams()).isEmpty();
        });
    }

    @Test
    void 존재하지_않는_챌린지_ID로_팀_목록_조회_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> challengeService.getTeamList(0L, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 팀_목록_조회_시_teamNumber가_올바르게_계산된다() {
        // given
        NewsletterGroup group = TestFixture.createNewsletterGroup("그룹");
        newsletterGroupRepository.save(group);
        Challenge challenge = TestFixture.createChallenge("챌린지", 1, today.plusDays(5), today.plusDays(15), group.getId());
        challengeRepository.save(challenge);

        ChallengeTeam team1 = TestFixture.createChallengeTeam(challenge.getId(), 50);
        ChallengeTeam team2 = TestFixture.createChallengeTeam(challenge.getId(), 60);
        ChallengeTeam team3 = TestFixture.createChallengeTeam(challenge.getId(), 70);
        ChallengeTeam team4 = TestFixture.createChallengeTeam(challenge.getId(), 80);
        challengeTeamRepository.saveAll(List.of(team1, team2, team3, team4));

        // when
        ChallengeTeamListResponse result = challengeService.getTeamList(challenge.getId(), member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.teams()).hasSize(4);
            softly.assertThat(result.teams().get(0).teamNumber()).isEqualTo(1);
            softly.assertThat(result.teams().get(1).teamNumber()).isEqualTo(2);
            softly.assertThat(result.teams().get(2).teamNumber()).isEqualTo(3);
            softly.assertThat(result.teams().get(3).teamNumber()).isEqualTo(4);
        });
    }
}
