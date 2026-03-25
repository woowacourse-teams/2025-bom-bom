package news.bombom.challenge.service;

import java.util.List;

public class ChallengeTodoReminderMessageTemplates {

    private ChallengeTodoReminderMessageTemplates() {}

    // 제목 템플릿: %1$s = 챌린지명, %2$d = 스트릭
    public static final String TITLE_0_FIRST    = "%1$s 오늘부터 함께해요. 아직 할 일이 남았어요!";
    public static final String TITLE_0_SECOND   = "[%1$s] 오늘부터 함께 습관을 쌓아봐요!";
    public static final String TITLE_2_FIRST = "[%1$s] %2$d일 연속! 이제 막 시작됐네요! 오늘도 이어가요!";
    public static final String TITLE_2_SECOND = "[%1$s] %2$d일 연속, 벌써 끊기면 아쉬워요! ⏰";
    public static final String TITLE_3_FIRST    = "[%1$s] %2$d일 연속 기록! 오늘도 이어가요!";
    public static final String TITLE_3_SECOND   = "[%1$s] %2$d일 연속 기록, 오늘 하지 않으면 스트릭이 끊겨요 🥲";
    public static final String TITLE_7_FIRST    = "[%1$s] 벌써 %2$d일 연속! 오늘도 같이 달려요 🏃";
    public static final String TITLE_7_SECOND   = "[%1$s] %2$d일 연속 기록, 오늘 놓치면 너무 아깝잖아요 🥲";
    public static final String TITLE_14_FIRST   = "[%1$s] %2$d일 연속이라니! 정말 멋져요! 👍";
    public static final String TITLE_14_SECOND  = "[%1$s] %2$d일 연속인데, 오늘 놓치시는거 아니죠? ⏰";
    public static final String TITLE_21_FIRST   = "[%1$s] %2$d일 연속! 완주가 코앞이에요. 정말 잘하고 있어요!";
    public static final String TITLE_21_SECOND  = "[%1$s] %2$d일 연속, 완주까지 얼마 안 남았어요 ⏰";
    public static final String TITLE_LAST_DAY_FIRST   = "[%1$s] 오늘이 마지막 날이에요! 끝까지 함께해요!";
    public static final String TITLE_LAST_DAY_SECOND  = "[%1$s] 마지막 날이에요. 오늘만 하면 완주예요 ⏰";

    // 본문 템플릿: %1$d = 스트릭, %2$d = 스트릭+1
    public static final List<String> LAST_DAY_BODY = List.of(
            "마지막 하루 남았어요. 오늘 읽으면 챌린지 완주예요 🎉",
            "5분만 읽으면 챌린지 완주! 오늘만 놓치면 아쉬워요 🏆"
    );

    public static final List<String> BODY_POOL = List.of(
            "오늘 읽으면 %2$d일 연속이에요. 같이 이어가요! ✨",
            "5분만 읽으면 오늘 출석이에요. 같이 해볼까요? 😄",
            "%1$d일 쌓아온 기록이에요. 조금만 힘내서 해볼까요? 💪",
            "오늘도 한 번만요. 5분이면 충분해요! ⏱️",
            "읽고 싶을 때 읽으면 돼요. 지금 한 번 어떨까요? 😉",
            "오늘 챌린지 아직 안 하셨죠? 딱 하나만 읽어봐요! 📖",
            "매일 조금씩 읽는 게 쌓이면 달라져요. 오늘도 함께해요! 📚",
            "바쁜 하루였겠지만, 5분만 투자해봐요. 오늘도 기록 남겨요! 📝",
            "%1$d일 쌓아온 기록이에요. 오늘 끊기면 아쉬워요 🥲",
            "읽고 싶을 때 읽으면 돼요. 지금 읽고 싶지 않나요? 😊",
            "오늘 읽으면 %2$d일 연속이에요! 🌱",
            "꾸준함이 쌓이고 있어요. 오늘 읽어서 %2$d일로 이어가요! 📚"
    );
}
