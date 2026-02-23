package me.bombom.api.v1.coupon.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private Member member;
    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        couponIssueRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);

        CouponIssue coupon1 = CouponIssue.of(
                member.getId(),
                "쿠폰A",
                "https://cdn.bombom.me/coupon-a.png"
        );
        CouponIssue coupon2 = CouponIssue.of(
                member.getId(),
                "쿠폰B",
                "https://cdn.bombom.me/coupon-b.png"
        );
        couponIssueRepository.save(coupon1);
        couponIssueRepository.save(coupon2);

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
    void 내가_받은_쿠폰_목록_조회_성공() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/coupon/issues/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].couponName").value(containsInAnyOrder("쿠폰A", "쿠폰B")))
                .andExpect(jsonPath("$[*].imageUrl").value(containsInAnyOrder(
                        "https://cdn.bombom.me/coupon-a.png",
                        "https://cdn.bombom.me/coupon-b.png"
                )))
                .andExpect(jsonPath("$[*].issuedAt").isNotEmpty());
    }

    @Test
    void 내가_받은_쿠폰이_없으면_빈배열_반환() throws Exception {
        // given
        couponIssueRepository.deleteAllInBatch();

        // when & then
        mockMvc.perform(get("/api/v1/coupon/issues/me")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
