package me.bombom.api.v1.challenge.dto.response;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ChallengeCommentResponseTest {

    @Test
    void 댓글_작성자_닉네임이_null이면_탈퇴한_사용자로_응답한다() {
        ChallengeCommentResponse response = new ChallengeCommentResponse(
                1L,
                null,
                null,
                "봄봄 뉴스레터",
                false,
                "아티클 제목",
                "인용문",
                "댓글",
                LocalDateTime.now(),
                false,
                0,
                false,
                0
        );

        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo("탈퇴한 사용자");
            softly.assertThat(response.profileImageUrl()).isNull();
        });
    }

    @Test
    void 댓글_작성자_닉네임이_있으면_기존_닉네임을_응답한다() {
        ChallengeCommentResponse response = new ChallengeCommentResponse(
                1L,
                "나밍곰",
                "profile.png",
                "봄봄 뉴스레터",
                true,
                "아티클 제목",
                "인용문",
                "댓글",
                LocalDateTime.now(),
                true,
                1,
                true,
                2
        );

        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo("나밍곰");
            softly.assertThat(response.profileImageUrl()).isEqualTo("profile.png");
        });
    }
}
