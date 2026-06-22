package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSubscriptionTrackRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.support.IntegrationTest;
import me.bombom.support.acceptance.AcceptanceTestHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class MaeilMailSubscribeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private NewsletterDetailRepository newsletterDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private MaeilMailSubscriptionTrackRepository trackRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestFixture.createUniqueMember("maeil-user", "maeil-subscribe-controller"));
        newsletterRepository.save(createMaeilMailNewsletter());
    }

    @Test
    void 미구독이면_빈_트랙을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions/native/maeil-mail")
                        .header(AcceptanceTestHeaders.MEMBER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks").isEmpty());
    }

    @Test
    void 트랙을_보내면_매일메일을_신규_구독한다() throws Exception {
        구독_변경(List.of("BE", "FE"))
                .andExpect(status().isOk());

        assertThat(subscribeRepository.findAll()).hasSize(1);
        assertThat(trackRepository.findAll())
                .extracting(MaeilMailSubscriptionTrack::getField)
                .containsExactlyInAnyOrder(MaeilMailTrack.BE, MaeilMailTrack.FE);
    }

    @Test
    void 구독_중인_트랙을_요청한_트랙으로_치환한다() throws Exception {
        구독_변경(List.of("BE", "FE")).andExpect(status().isOk());
        구독_변경(List.of("BE")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/subscriptions/native/maeil-mail")
                        .header(AcceptanceTestHeaders.MEMBER_ID, member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks.length()").value(1))
                .andExpect(jsonPath("$.tracks[0]").value("BE"));
    }

    @Test
    void 구독을_삭제하면_구독과_트랙을_모두_삭제한다() throws Exception {
        구독_변경(List.of("BE", "FE")).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/subscriptions/native/maeil-mail")
                        .header(AcceptanceTestHeaders.MEMBER_ID, member.getId()))
                .andExpect(status().isOk());

        assertThat(subscribeRepository.findAll()).isEmpty();
        assertThat(trackRepository.findAll()).isEmpty();
    }

    @Test
    void 미구독_상태의_삭제는_성공한다() throws Exception {
        mockMvc.perform(delete("/api/v1/subscriptions/native/maeil-mail")
                        .header(AcceptanceTestHeaders.MEMBER_ID, member.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void 빈_트랙으로_구독을_요청하면_400을_반환한다() throws Exception {
        구독_변경(List.of())
                .andExpect(status().isBadRequest());

        assertThat(subscribeRepository.findAll()).isEmpty();
    }

    @Test
    void 중복된_트랙으로_구독을_요청하면_400을_반환한다() throws Exception {
        구독_변경(List.of("BE", "BE"))
                .andExpect(status().isBadRequest());

        assertThat(subscribeRepository.findAll()).isEmpty();
    }

    private org.springframework.test.web.servlet.ResultActions 구독_변경(List<String> tracks) throws Exception {
        return mockMvc.perform(put("/api/v1/subscriptions/native/maeil-mail")
                .header(AcceptanceTestHeaders.MEMBER_ID, member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("tracks", tracks))));
    }

    private Newsletter createMaeilMailNewsletter() {
        Category category = categoryRepository.save(TestFixture.createCategory());
        NewsletterDetail detail = newsletterDetailRepository.save(TestFixture.createNewsletterDetail(true));
        return TestFixture.createNewsletter(
                "매일메일",
                "maeil@bombom.news",
                category.getId(),
                detail.getId(),
                NewsletterSource.MAEIL_MAIL
        );
    }
}
