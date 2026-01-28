package me.bombom.api.v1.subscribe.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.RetryableException;
import me.bombom.api.v1.subscribe.config.SubscribePatternProperties;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class UnsubscribeAgent {

    private static final String ALL_URLS_PATTERN = "**/*";
    private static final Set<String> BLOCKED_RESOURCE_TYPES = Set.of("image", "font", "media");

    private final SubscribePatternProperties properties;

    private static final long UNSUBSCRIBE_TIMEOUT_MS = 10000;
    private static final long POLLING_INTERVAL_MS = 500;

    public boolean unsubscribe(String url, Long newsletterId) {
        AtomicBoolean hasError = new AtomicBoolean(false);
        AtomicBoolean isProcessed = new AtomicBoolean(false);
        AtomicBoolean isReady = new AtomicBoolean(false);

        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(createLaunchOptions())) {
            BrowserContext context = getBrowserContext(browser);
            Page page = context.newPage();

            // 핸들러 등록
            setupDialogHandler(page, hasError, newsletterId);
            setupResponseHandler(page, hasError, isProcessed, isReady, newsletterId);

            page.navigate(url);
            // 페이지 로딩 느릴 경우 대응 (성공 메시지 or 버튼/링크 대기)
            waitForContent(page);

            // 1. 즉시 성공 케이스 (URL 접속만으로 취소됨)
            if (isUnsubscribeSuccess(page)) {
                log.info("구독 취소 성공 (즉시 성공) - newsletterId: {}", newsletterId);
                return !hasError.get();
            }
            // 2. 버튼 클릭 필요 케이스
            return processUnsubscribeAction(page, isProcessed, isReady, hasError, newsletterId);
        } catch (AutoUnsubscribeFailedException | RetryableException e) {
            throw e;
        } catch (PlaywrightException e) {
            throw new RetryableException("브라우저 실행 중 오류 발생", e);
        } catch (Exception e) {
            log.error("구독 취소 중 예상치 못한 오류 - newsletterId: {}, URL: {}", newsletterId, url, e);
            throw new AutoUnsubscribeFailedException("예상치 못한 오류 발생: " + e.getMessage(), newsletterId, url);
        }
    }

    private LaunchOptions createLaunchOptions() {
        return new LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                        "--no-sandbox",
                        "--disable-setuid-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--disable-extensions",
                        "--disable-popup-blocking",
                        "--no-first-run",
                        "--no-default-browser-check",
                        "--disable-translate",
                        "--disable-background-networking",
                        "--disable-sync"));
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

    private void setupDialogHandler(Page page, AtomicBoolean hasError, Long newsletterId) {
        page.onDialog(dialog -> {
            String message = dialog.message();
            Matcher matcher = properties.getErrorPattern().matcher(message);
            if (matcher.find()) {
                hasError.set(true);
            }
            dialog.accept();
        });
    }

    private void setupResponseHandler(
            Page page,
            AtomicBoolean hasError,
            AtomicBoolean isProcessed,
            AtomicBoolean isReady,
            Long newsletterId
    ) {
        page.onResponse(response -> {
            if (!isReady.get()) {
                return;
            }

            int status = response.status();
            String url = response.url();
            if (isAdUrl(url)) {
                return;
            }

            if (status >= 200 && status < 400 && url.contains("unsubscribe")) {
                log.info("구독 취소 성공 응답 감지 (HTTP {}) - newsletterId: {}", status, newsletterId);
                isProcessed.set(true);
            } else if (status >= 400 && status < 500 && url.contains("unsubscribe")) {
                String body = response.text();
                if (properties.getAlreadyUnsubscribedPattern().matcher(body).find()) {
                    log.info("이미 구독 취소됨 (HTTP {}) - newsletterId: {}", status, newsletterId);
                    isProcessed.set(true);
                } else {
                    hasError.set(true);
                }
            } else if (status >= 500 && url.contains("unsubscribe")) {
                throw new RetryableException("서버 오류 (HTTP " + status + ")");
            }
        });
    }

    private void waitForContent(Page page) {
        Locator successLocator = page.getByText(properties.getSuccessPattern());
        Locator buttonLocator = page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName(properties.getUnsubscribePattern()));
        Locator linkLocator = page.getByRole(AriaRole.LINK, new GetByRoleOptions().setName(properties.getUnsubscribePattern()));

        try {
            Locator combined = successLocator.or(buttonLocator).or(linkLocator);
            combined.waitFor(new Locator.WaitForOptions().setTimeout(UNSUBSCRIBE_TIMEOUT_MS));
        } catch (TimeoutError e) {
            // 타임아웃 되더라도 확인 로직 진행 (페이지가 느릴 수 있음). 이후 로직에서 다시 확인해서 처리 X
        }
    }

    private boolean isUnsubscribeSuccess(Page page) {
        return page.getByText(properties.getSuccessPattern()).count() > 0;
    }

    private boolean processUnsubscribeAction(
            Page page,
            AtomicBoolean isProcessed,
            AtomicBoolean isReady,
            AtomicBoolean hasError,
            Long newsletterId
    ) {
        Locator confirmButton = findUnsubscribeButton(page);
        if (confirmButton == null || !confirmButton.isVisible()) {
            throw new AutoUnsubscribeFailedException("구독 취소 버튼을 찾을 수 없습니다", newsletterId, page.url());
        }

        isProcessed.set(false);
        isReady.set(true);
        String beforeUrl = page.url();

        confirmButton.click();
        return waitForResult(page, isProcessed, hasError, beforeUrl, newsletterId);
    }

    private Locator findUnsubscribeButton(Page page) {
        for (AriaRole role : List.of(AriaRole.BUTTON, AriaRole.LINK)) {
            Locator button = page.getByRole(role, new GetByRoleOptions().setName(properties.getUnsubscribePattern()));
            if (button.isVisible()) {
                return button;
            }
        }
        return null;
    }

    private boolean waitForResult(
            Page page,
            AtomicBoolean isProcessed,
            AtomicBoolean hasError,
            String beforeUrl,
            Long newsletterId
    ) {
        long deadline = System.currentTimeMillis() + UNSUBSCRIBE_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            // setupResponseHandler에서 설정한 핸들러 처리를 대기
            if (isProcessed.get() || hasError.get()) {
                break;
            }
            // 페이지 URL이 변경되었다면 성공으로 간주 (리다이렉트 발생)
            if (!page.url().equals(beforeUrl)) {
                log.info("구독 취소 성공 (페이지 이동 감지) - newsletterId: {}", newsletterId);
                return true;
            }
            page.waitForTimeout(POLLING_INTERVAL_MS);
        }

        if (isProcessed.get()) {
            return true;
        }

        if (!hasError.get()) {
            log.info("구독 취소 성공 (에러 없음) - newsletterId: {}", newsletterId);
            return true;
        }

        // hasError가 true인 경우 = 에러 다이얼로그 또는 HTTP 4xx 에러 감지됨
        throw new AutoUnsubscribeFailedException("구독 취소 중 에러가 감지되었습니다", newsletterId, page.url());
    }

    private boolean isAdUrl(String url) {
        return properties.getAdDomains().stream()
                .anyMatch(url::contains);
    }
}
