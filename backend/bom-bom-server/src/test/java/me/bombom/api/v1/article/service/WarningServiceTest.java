package me.bombom.api.v1.article.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.WarningSetting;
import me.bombom.api.v1.article.dto.response.WarningSettingResponse;
import me.bombom.api.v1.article.repository.WarningSettingRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class WarningServiceTest {

    @Autowired
    private WarningService warningService;

    @Autowired
    private WarningSettingRepository warningSettingRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        warningSettingRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        WarningSetting warningSetting = WarningSetting.builder()
                .memberId(member.getId())
                .isVisible(true)
                .build();
        warningSettingRepository.save(warningSetting);
    }

    @Test
    void 경고_설정_조회_성공() {
        // when
        WarningSettingResponse response = warningService.getCapacityWarningStatus(member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response).isNotNull();
            softly.assertThat(response.status()).isTrue();
        });
    }
}
