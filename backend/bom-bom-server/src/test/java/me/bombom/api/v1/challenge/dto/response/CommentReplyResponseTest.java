package me.bombom.api.v1.challenge.dto.response;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CommentReplyResponseTest {

    @Test
    void 답글_작성자_닉네임이_null이면_탈퇴한_사용자로_응답한다() {
        CommentReplyResponse response = new CommentReplyResponse(
                1L,
                null,
                null,
                "답글",
                LocalDateTime.now(),
                false,
                false
        );

        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo("탈퇴한 사용자");
            softly.assertThat(response.profileImageUrl()).isNull();
        });
    }

    @Test
    void 답글_작성자_닉네임이_있으면_기존_닉네임을_응답한다() {
        CommentReplyResponse response = new CommentReplyResponse(
                1L,
                "나밍곰",
                "profile.png",
                "답글",
                LocalDateTime.now(),
                true,
                true
        );

        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo("나밍곰");
            softly.assertThat(response.profileImageUrl()).isEqualTo("profile.png");
        });
    }
}
