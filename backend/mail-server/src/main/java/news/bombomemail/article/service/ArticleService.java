package news.bombomemail.article.service;

import jakarta.mail.Address;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.domain.Article;
import news.bombomemail.article.repository.ArticleRepository;
import news.bombomemail.article.util.ReadingTimeCalculator;
import news.bombomemail.article.util.SummaryGenerator;
import news.bombomemail.article.util.UnsubscribeUrlExtractor;
import news.bombomemail.member.domain.Member;
import news.bombomemail.member.repository.MemberRepository;
import news.bombomemail.newsletter.domain.Newsletter;
import news.bombomemail.newsletter.repository.NewsletterRepository;
import news.bombomemail.newsletter.repository.NewsletterVerificationRepository;
import news.bombomemail.reading.event.TodayReadingEvent;
import news.bombomemail.article.event.ArticleArrivedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NewsletterVerificationRepository newsletterVerificationRepository;
    private final UnsubscribeUrlExtractor unsubscribeUrlExtractor;

    @Transactional
    public boolean save(MimeMessage message, String contents) throws MessagingException, DataAccessException {
        Member member = resolveMember(message);
        if (member == null) {
            return false;
        }

        Newsletter newsletter = resolveNewsletter(message);
        if (newsletter == null) {
            return false;
        }

        Article article = articleRepository.save(buildArticle(message, contents, member, newsletter));
        String unsubscribeUrl = unsubscribeUrlExtractor.extract(article.getContents());

        applicationEventPublisher.publishEvent(ArticleArrivedEvent.of(newsletter.getId(), member.getId(), unsubscribeUrl));
        applicationEventPublisher.publishEvent(TodayReadingEvent.from(member.getId()));

        return true;
    }

    private Member resolveMember(MimeMessage message) throws MessagingException {
        Address[] toRecipients = message.getRecipients(RecipientType.TO);
        if (toRecipients == null || toRecipients.length == 0) {
            log.info("받는이가 존재하지 않아 메일을 폐기합니다: {}", message.getSubject());
            return null;
        }

        String toEmailAddress = ((InternetAddress) toRecipients[0]).getAddress();
        Optional<Member> optionalMember = memberRepository.findByEmail(toEmailAddress);
        if (optionalMember.isEmpty()) {
            log.info("미등록 수신자({})라 메일을 폐기합니다: {}", toEmailAddress, message.getSubject());
            return null;
        }
        return optionalMember.get();
    }

    private Newsletter resolveNewsletter(MimeMessage message) throws MessagingException {
        String fromEmailAddress = Optional.ofNullable(message.getFrom())
                .stream()
                .flatMap(Arrays::stream)
                .filter(InternetAddress.class::isInstance)
                .map(InternetAddress.class::cast)
                .map(InternetAddress::getAddress)
                .findFirst()
                .orElse(null);

        if (fromEmailAddress == null) {
            log.info("보낸이가 존재하지 않아 메일을 폐기합니다: {}", message.getSubject());
            return null;
        }

        String normalizedFromEmail = fromEmailAddress.strip().toLowerCase();
        return findNewsletterByEmail(normalizedFromEmail)
                .orElseGet(() -> {
                    log.info("미등록 발신자({})라 메일 폐기합니다", normalizedFromEmail);
                    return null;
                });
    }

    private Optional<Newsletter> findNewsletterByEmail(String fromEmailAddress) {
        return newsletterRepository.findByEmail(fromEmailAddress)
                .or(() -> newsletterVerificationRepository.findByEmail(fromEmailAddress)
                        .flatMap(verification -> newsletterRepository.findById(verification.getNewsletterId())));
    }

    private Article buildArticle(
            MimeMessage message,
            String contents,
            Member member,
            Newsletter newsletter
    ) throws MessagingException {
        return Article.builder()
                .title(message.getSubject())
                .contents(contents)
                .expectedReadTime(ReadingTimeCalculator.calculate(contents))
                .contentsSummary(SummaryGenerator.summarize(contents))
                .memberId(member.getId())
                .newsletterId(newsletter.getId())
                .arrivedDateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }
}
