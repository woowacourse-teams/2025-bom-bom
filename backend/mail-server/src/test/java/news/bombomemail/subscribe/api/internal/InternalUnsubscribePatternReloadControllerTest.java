package news.bombomemail.subscribe.api.internal;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import news.bombomemail.common.internal.api.InternalApiKeyInterceptor;
import news.bombomemail.common.internal.api.InternalApiWebConfig;
import news.bombomemail.subscribe.service.UnsubscribePatternReloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InternalUnsubscribePatternReloadController.class)
@ContextConfiguration(classes = InternalUnsubscribePatternReloadControllerTest.TestApplication.class)
@Import({InternalApiWebConfig.class, InternalApiKeyInterceptor.class})
@TestPropertySource(properties = "MAIL_SERVER_INTERNAL_API_KEY=test-secret")
class InternalUnsubscribePatternReloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnsubscribePatternReloadService unsubscribePatternReloadService;

    @Test
    void 올바른_internal_api_key로_리로드를_호출한다() throws Exception {
        mockMvc.perform(post("/internal/v1/unsubscribe-patterns/reload")
                        .header(InternalApiKeyInterceptor.INTERNAL_API_KEY_HEADER, "test-secret"))
                .andExpect(status().isNoContent());

        verify(unsubscribePatternReloadService).reload();
    }

    @Test
    void internal_api_key가_없으면_unauthorized를_반환한다() throws Exception {
        mockMvc.perform(post("/internal/v1/unsubscribe-patterns/reload"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 잘못된_internal_api_key면_unauthorized를_반환한다() throws Exception {
        mockMvc.perform(post("/internal/v1/unsubscribe-patterns/reload")
                        .header(InternalApiKeyInterceptor.INTERNAL_API_KEY_HEADER, "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackageClasses = InternalUnsubscribePatternReloadController.class)
    static class TestApplication {
    }
}
