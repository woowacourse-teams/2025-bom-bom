package news.bombomemail.email.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import news.bombomemail.article.domain.Article;
import news.bombomemail.article.repository.ArticleRepository;
import news.bombomemail.article.service.ArticleService;
import news.bombomemail.article.util.UnsubscribeUrlExtractor;
import news.bombomemail.email.EmailConfig;
import news.bombomemail.member.domain.Member;
import news.bombomemail.member.repository.MemberRepository;
import news.bombomemail.member.domain.Gender;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.nio.file.StandardCopyOption;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.SoftAssertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({EmailService.class, EmailConfig.class, ArticleService.class, UnsubscribeUrlExtractor.class})
class EmailServiceTest {

    @Autowired
    EmailService emailService;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    NewsletterRepository newsletterRepository;

    @BeforeEach
    void setup() {
        memberRepository.save(Member.builder()
            .email("test-member@example.com")
            .nickname("테스트멤버")
            .providerId("test")
            .provider("google")
            .gender(Gender.MALE)
            .roleId(1L)
            .birthDate(java.time.LocalDate.of(1990, 1, 1)) // 생년월일 추가
            .build());

        newsletterRepository.save(Newsletter.builder()
            .name("테스트뉴스레터")
            .description("설명")
            .imageUrl("이미지")
            .email("test-newsletter@example.com")
            .categoryId(1L)
            .detailId(1L)
            .build());
    }

    @TempDir
    Path tempDir;

    @Test
    void eml_파일을_파싱하여_Article로_저장한다() throws Exception {
        // given
        File emlFile = copySampleEmlToTemp();

        // when
        emailService.processEmailFile(emlFile);

        // then
        assertThat(articleRepository.findAll()).isNotEmpty();
    }

    @Test
    void eml_파일_파싱_결과를_Article에_올바르게_저장한다() throws Exception {
        // given
        File emlFile = copySampleEmlToTemp();

        // when
        emailService.processEmailFile(emlFile);

        // then
        List<Article> articles = articleRepository.findAll();
        assertThat(articles).hasSize(1);
        Article article = articles.get(0);
        assertSoftly(soft -> {
            soft.assertThat(article.getTitle()).isEqualTo("테스트 이메일 제목");
            soft.assertThat(article.getContents()).contains("이것은 테스트용 이메일 본문입니다");
        });


    }

    @Test
    void 제목이_없는_eml_파일은_Article로_저장되지_않는다() throws Exception {
        // given
        Path source = Path.of("src/test/resources/sample_no_subject.eml");
        Path target = tempDir.resolve("sample_no_subject.eml");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        File emlFile = target.toFile();

        // when
        emailService.processEmailFile(emlFile);

        // then
        assertThat(articleRepository.findAll()).isEmpty();
    }

    @Test
    void 본문이_없는_eml_파일은_contents가_빈_문자열로_저장된다() throws Exception {
        // given
        Path source = Path.of("src/test/resources/sample_no_body.eml");
        Path target = tempDir.resolve("sample_no_body.eml");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        File emlFile = target.toFile();


        // when
        emailService.processEmailFile(emlFile);

        // then
        List<Article> articles = articleRepository.findAll();
        assertSoftly(soft -> {
            soft.assertThat(articles).hasSize(1);
            soft.assertThat(articles.get(0).getContents()).isEmpty();
        });


    }

    @Test
    void 잘못된_eml_파일은_Article로_저장되지_않는다() throws Exception {
        // given
        Path source = Path.of("src/test/resources/sample_invalid.eml");
        Path target = tempDir.resolve("sample_invalid.eml");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        File emlFile = target.toFile();

        // when
        emailService.processEmailFile(emlFile);

        // then
        assertThat(articleRepository.findAll()).isEmpty();
    }

    @Test
    void 여러_개의_eml_파일을_연속_처리하면_모두_Article로_저장된다() throws Exception {
        // given
        Path source1 = Path.of("src/test/resources/sample_multi_1.eml");
        Path target1 = tempDir.resolve("sample_multi_1.eml");
        Files.copy(source1, target1, StandardCopyOption.REPLACE_EXISTING);
        File emlFile1 = target1.toFile();

        Path source2 = Path.of("src/test/resources/sample_multi_2.eml");
        Path target2 = tempDir.resolve("sample_multi_2.eml");
        Files.copy(source2, target2, StandardCopyOption.REPLACE_EXISTING);
        File emlFile2 = target2.toFile();

        // when
        emailService.processEmailFile(emlFile1);
        emailService.processEmailFile(emlFile2);

        // then
        List<Article> articles = articleRepository.findAll();
        assertSoftly(soft -> {
            assertThat(articles).hasSize(2);
            assertThat(articles.get(0).getTitle()).isIn("첫번째 메일", "두번째 메일");
            assertThat(articles.get(1).getTitle()).isIn("첫번째 메일", "두번째 메일");
        });
    }

    @Test
    void 미등록_수신자_저장안됨() throws Exception {
        // given
        Path file = tempDir.resolve("no_recipient.eml");
        String eml = String.join("\r\n",
                "From: test-newsletter@example.com",
                "Subject: 제목만있음",
                "",
                "본문");
        Files.writeString(file, eml, StandardOpenOption.CREATE);

        // when
        emailService.processEmailFile(file.toFile());

        // then
        assertThat(articleRepository.findAll()).isEmpty();
    }

    @Test
    void 미등록_발신자_저장안됨() throws Exception {
        // given
        Path file = tempDir.resolve("unknown_newsletter.eml");
        String eml = String.join("\r\n",
                "From: unknown@example.com",
                "To: test-member@example.com",
                "Subject: 테스트",
                "",
                "본문");
        Files.writeString(file, eml, StandardOpenOption.CREATE);

        // when
        emailService.processEmailFile(file.toFile());

        // then
        assertThat(articleRepository.findAll()).isEmpty();
    }

    @Test
    void IO오류시_처리된다() {
        // given
        File dir = tempDir.toFile();

        // when & then
        assertSoftly(soft -> {
            assertThatCode(() -> emailService.processEmailFile(dir))
                    .doesNotThrowAnyException();
            assertThat(articleRepository.findAll()).isEmpty();
        });
    }

    @Test
    void 이메일_처리시_아티클이_저장되고_이벤트가_발생한다() throws Exception {
        // given
        File emlFile = copySampleEmlToTemp();

        // when
        emailService.processEmailFile(emlFile);

        // then
        assertSoftly(softly -> {
            softly.assertThat(articleRepository.findAll()).hasSize(1);
            Article article = articleRepository.findAll().get(0);
            softly.assertThat(article.getTitle()).isEqualTo("테스트 이메일 제목");
            softly.assertThat(article.getContents()).contains("이것은 테스트용 이메일 본문입니다");
        });
    }

    private File copySampleEmlToTemp() throws Exception {
        Path source = Path.of("src/test/resources/sample.eml");
        Path target = tempDir.resolve("sample.eml");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target.toFile();
    }
}
