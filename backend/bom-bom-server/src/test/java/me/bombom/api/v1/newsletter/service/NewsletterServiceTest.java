package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NewsletterServiceTest {

    @Autowired
    private NewsletterService newsletterService;

    @Autowired
    private NewsletterRepository newsletterRepository;

    List<Newsletter> newsletters;

    @BeforeEach
    public void setup() {
        newsletters = List.of(
                Newsletter.builder()
                        .name("뉴스픽")
                        .email("news@newspick.com")
                        .imageUrl("https://cdn.bombom.me/images/news1.png")
                        .description("하루 5분, 세상의 주요 뉴스를 요약해드립니다.")
                        .mainUrl("https://newspick.com")
                        .subscribeUrl("https://newspick.com/subscribe")
                        .issueCycle("매일 아침")
                        .categoryId(1L)
                        .subscribeCount(950)
                        .build(),

                Newsletter.builder()
                        .name("IT타임즈")
                        .email("editor@ittimes.io")
                        .imageUrl("https://cdn.bombom.me/images/news2.png")
                        .description("IT 이슈와 트렌드를 주간 단위로 전합니다.")
                        .mainUrl("https://ittimes.io")
                        .subscribeUrl("https://ittimes.io/subscribe")
                        .issueCycle("매주 월요일")
                        .categoryId(2L)
                        .subscribeCount(770)
                        .build(),

                Newsletter.builder()
                        .name("비즈레터")
                        .email("biz@bizletter.com")
                        .imageUrl("https://cdn.bombom.me/images/news3.png")
                        .description("비즈니스 인사이트를 쉽게 전달합니다.")
                        .mainUrl("https://bizletter.com")
                        .subscribeUrl("https://bizletter.com/subscribe")
                        .issueCycle("격주 화요일")
                        .categoryId(3L)
                        .subscribeCount(620)
                        .build(),

                Newsletter.builder()
                        .name("헬스레터")
                        .email("health@healthletter.kr")
                        .imageUrl("https://cdn.bombom.me/images/news4.png")
                        .description("건강 정보와 운동 팁을 소개합니다.")
                        .mainUrl("https://healthletter.kr")
                        .subscribeUrl("https://healthletter.kr/join")
                        .issueCycle("매주 토요일")
                        .categoryId(4L)
                        .subscribeCount(540)
                        .build(),

                Newsletter.builder()
                        .name("브랜드다움")
                        .email("brand@daum.com")
                        .imageUrl("https://cdn.bombom.me/images/news5.png")
                        .description("브랜딩과 마케팅 전략을 공유합니다.")
                        .mainUrl("https://branddaum.com")
                        .subscribeUrl("https://branddaum.com/join")
                        .issueCycle("매주 금요일")
                        .categoryId(3L)
                        .subscribeCount(810)
                        .build(),

                Newsletter.builder()
                        .name("테크투데이")
                        .email("tech@today.io")
                        .imageUrl("https://cdn.bombom.me/images/news6.png")
                        .description("최신 테크 소식을 큐레이션합니다.")
                        .mainUrl("https://techtoday.io")
                        .subscribeUrl("https://techtoday.io/subscribe")
                        .issueCycle("매일")
                        .categoryId(2L)
                        .subscribeCount(880)
                        .build(),

                Newsletter.builder()
                        .name("UX Digest")
                        .email("ux@digest.io")
                        .imageUrl("https://cdn.bombom.me/images/news7.png")
                        .description("UX 관련 아티클과 인사이트를 전달합니다.")
                        .mainUrl("https://uxdigest.io")
                        .subscribeUrl("https://uxdigest.io/join")
                        .issueCycle("매주 목요일")
                        .categoryId(5L)
                        .subscribeCount(400)
                        .build(),

                Newsletter.builder()
                        .name("정치읽기")
                        .email("info@readpolitics.org")
                        .imageUrl("https://cdn.bombom.me/images/news8.png")
                        .description("정치 이슈를 쉽게 설명해드립니다.")
                        .mainUrl("https://readpolitics.org")
                        .subscribeUrl("https://readpolitics.org/subscribe")
                        .issueCycle("매주 일요일")
                        .categoryId(5L)
                        .subscribeCount(510)
                        .build(),

                Newsletter.builder()
                        .name("아트위클리")
                        .email("art@weekly.net")
                        .imageUrl("https://cdn.bombom.me/images/news9.png")
                        .description("예술 전시와 뉴스를 큐레이션합니다.")
                        .mainUrl("https://artweekly.net")
                        .subscribeUrl("https://artweekly.net/subscribe")
                        .issueCycle("격주 목요일")
                        .categoryId(4L)
                        .subscribeCount(350)
                        .build(),

                Newsletter.builder()
                        .name("개발자 다이어리")
                        .email("dev@diary.dev")
                        .imageUrl("https://cdn.bombom.me/images/news10.png")
                        .description("개발자가 직접 전하는 실무 팁")
                        .mainUrl("https://diary.dev")
                        .subscribeUrl("https://diary.dev/subscribe")
                        .issueCycle("매주 수요일")
                        .categoryId(1L)
                        .subscribeCount(770)
                        .build()
        );

        newsletterRepository.saveAll(newsletters);
    }

    @Test
    void 뉴스레터를_모두_조회할_수_있다() {
        //when
        List<NewsletterResponse> result = newsletterService.getNewsletters();

        //then
        assertThat(result.size()).isEqualTo(newsletters.size());
    }
}