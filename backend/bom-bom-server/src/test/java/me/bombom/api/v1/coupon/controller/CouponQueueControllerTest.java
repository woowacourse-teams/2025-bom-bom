package me.bombom.api.v1.coupon.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.coupon.repository.CouponQueueRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "coupon.events[0].name=day1-coupon",
        "coupon.events[0].max-count=3",
        "coupon.events[0].batch-size=50",
        "coupon.events[0].active-limit=2",
        "coupon.events[0].active-ttl-seconds=30",
        "coupon.events[0].polling-interval-seconds=3",
        "coupon.events[0].image-url=https://example.com/coupon.png",
        "spring.task.scheduling.enabled=false"
})
class CouponQueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponQueueRepository couponQueueRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member member;
    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        couponIssueRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();

        member = TestFixture.createUniqueMember("member-1", "provider-1");
        memberRepository.save(member);
        couponIssueRepository.saveAll(TestFixture.createCouponPool("day1-coupon", 1, "https://example.com/coupon.png"));

        Map<String, Object> attributes = Map.of(
                "id", member.getId().toString(),
                "email", member.getEmail(),
                "name", member.getNickname()
        );
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
        authToken = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "registrationId"
        );
    }

    @Test
    void 대기열_등록_성공() throws Exception {
        // given
        // when, then
        mockMvc.perform(post("/api/v1/coupons/day1-coupon/queue-entries")
                        .with(authentication(authToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.couponName").value("day1-coupon"))
                .andExpect(jsonPath("$.status").value(Matchers.anyOf(Matchers.is("WAITING"), Matchers.is("ACTIVE"))));
    }

    @Test
    void 대기열_상태_조회_성공() throws Exception {
        // given
        couponQueueRepository.addIfAbsentQueue("day1-coupon", member.getId(), System.currentTimeMillis());
        // when, then
        mockMvc.perform(get("/api/v1/coupons/day1-coupon/queue-entries/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponName").value("day1-coupon"))
                .andExpect(jsonPath("$.status").value(Matchers.anyOf(Matchers.is("WAITING"), Matchers.is("ACTIVE"))));
    }

    @Test
    void 발급_확정_성공() throws Exception {
        // given
        long expireAt = System.currentTimeMillis() + 30_000;
        couponQueueRepository.addActive("day1-coupon", member.getId(), expireAt);
        // when, then
        mockMvc.perform(post("/api/v1/coupons/day1-coupon/issues")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/coupon.png"))
                .andExpect(jsonPath("$.issuedAt").exists());
    }

    @Test
    void 발급_내역_조회_성공() throws Exception {
        // given
        couponIssueRepository.save(CouponIssue.of(member.getId(), "day1-coupon", "https://example.com/coupon.png"));
        // when, then
        mockMvc.perform(get("/api/v1/coupons/issues/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].couponName").value("day1-coupon"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://example.com/coupon.png"))
                .andExpect(jsonPath("$[0].issuedAt").exists());
    }
}
