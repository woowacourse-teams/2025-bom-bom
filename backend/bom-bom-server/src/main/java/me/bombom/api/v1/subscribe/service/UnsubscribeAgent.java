package me.bombom.api.v1.subscribe.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(propagation = Propagation.NEVER)
public class UnsubscribeAgent {

    private static final String ALL_URLS_PATTERN = "**/*";
    private static final Set<String> BLOCKED_RESOURCE_TYPES = Set.of("image", "font", "media");
    private static final Pattern UNSUBSCRIBE_PATTERN = Pattern.compile(
            "unsubscribe|구독.?취소|수신.?거부|cancel|confirm|yes",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SUCCESS_PATTERN = Pattern.compile(
            "success|unsubscribed|canceled|cancelled|취소.?완료|처리.?완료|해지.?완료|거부.?완료|취소.?되었습니다",
            Pattern.CASE_INSENSITIVE
    );

    @Async
    public void unsubscribe(String url) {
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new LaunchOptions().setHeadless(true))) {

            BrowserContext context = getBrowserContext(browser);
            Page page = context.newPage();
            page.navigate(url);
            // 이미 구독 취소 | URL 클릭 만으로 취소 성공
            if (isUnsubscribeSuccess(page)) {
                return;
            }

            // 취소/확인 버튼 찾기 및 클릭
            Locator confirmButton = findUnsubscribeButton(page);
            if (confirmButton != null && confirmButton.isVisible()) {
                confirmButton.click();
            } else {
                log.warn("구독 취소 실패: 버튼을 찾지 못했고 성공 메시지도 없습니다.");
            }
            page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(5000));
        } catch (Exception e) {
            log.error("구독 취소 실패: {}", e.getMessage(), e);
        }
    }

    private BrowserContext getBrowserContext(Browser browser) {
        BrowserContext context = browser.newContext();
        // 이미지, 폰트 로딩 차단 (부하 감소)
        context.route(ALL_URLS_PATTERN, route -> {
            String type = route.request().resourceType();
            if (BLOCKED_RESOURCE_TYPES.contains(type)) {
                route.abort();
            } else {
                route.resume();
            }
        });
        return context;
    }

    private Locator findUnsubscribeButton(Page page) {
        for (AriaRole role : List.of(AriaRole.BUTTON, AriaRole.LINK)) {
            Locator button = page.getByRole(role, new GetByRoleOptions().setName(UNSUBSCRIBE_PATTERN));
            if (button.isVisible()) {
                return button;
            }
        }
        return null;
    }

    private boolean isUnsubscribeSuccess(Page page) {
        return page.getByText(SUCCESS_PATTERN).isVisible();
    }
}
