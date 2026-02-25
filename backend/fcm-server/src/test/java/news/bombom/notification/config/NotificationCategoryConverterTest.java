package news.bombom.notification.config;

import static org.assertj.core.api.Assertions.assertThat;

import news.bombom.notification.domain.NotificationCategory;
import org.junit.jupiter.api.Test;

class NotificationCategoryConverterTest {

    private final NotificationCategoryConverter converter = new NotificationCategoryConverter();

    @Test
    void kebab_case_카테고리를_enum으로_변환한다() {
        NotificationCategory result = converter.convert("challenge-todo-reminder");

        assertThat(result).isEqualTo(NotificationCategory.CHALLENGE_TODO_REMINDER);
    }

    @Test
    void snake_case_카테고리를_enum으로_변환한다() {
        NotificationCategory result = converter.convert("challenge_todo_reminder");

        assertThat(result).isEqualTo(NotificationCategory.CHALLENGE_TODO_REMINDER);
    }

    @Test
    void 대소문자_섞인_카테고리를_enum으로_변환한다() {
        NotificationCategory result = converter.convert("aRtIcLe");

        assertThat(result).isEqualTo(NotificationCategory.ARTICLE);
    }
}
