package news.bombomemail.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import news.bombomemail.article.repository.ReceivedMailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.util.ReflectionTestUtils;

class ReceivedMailServiceTest {

    ReceivedMailRepository receivedMailRepository = mock(ReceivedMailRepository.class);

    ReceivedMailService receivedMailService = new ReceivedMailService(receivedMailRepository);

    @Test
    void 설정이_꺼져_있으면_중복_확인_키를_만들지_않는다() throws Exception {
        // given
        ReflectionTestUtils.setField(receivedMailService, "enabled", false);

        // when
        receivedMailService.disableIfTableMissing();

        // then
        assertThat(receivedMailService.keyOf(1L, messageWithId("<a@example>"))).isEmpty();
        verifyNoInteractions(receivedMailRepository);
    }

    @Test
    void 설정을_켰는데_테이블이_없으면_중복_확인을_끄고_메일을_그대로_받는다() throws Exception {
        // given
        ReflectionTestUtils.setField(receivedMailService, "enabled", true);
        when(receivedMailRepository.count())
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'received_mail' doesn't exist"));

        // when
        receivedMailService.disableIfTableMissing();

        // then
        assertThat(receivedMailService.isEnabled()).isFalse();
        assertThat(receivedMailService.keyOf(1L, messageWithId("<a@example>"))).isEmpty();
    }

    @Test
    void 빈_Message_ID는_없는_것으로_본다() throws Exception {
        // given
        ReflectionTestUtils.setField(receivedMailService, "enabled", true);

        // when & then
        assertThat(receivedMailService.keyOf(1L, messageWithId("<>"))).isEmpty();
        assertThat(receivedMailService.keyOf(1L, messageWithId("  "))).isEmpty();
    }

    @Test
    void Message_ID는_SHA_256_32바이트로_저장한다() throws Exception {
        // given
        ReflectionTestUtils.setField(receivedMailService, "enabled", true);

        // when
        byte[] hash = receivedMailService.keyOf(1L, messageWithId("<a@example>")).orElseThrow().messageIdHash();

        // then
        assertThat(hash).hasSize(32);
    }

    private MimeMessage messageWithId(String messageId) throws Exception {
        MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setHeader("Message-ID", messageId);
        return message;
    }
}
