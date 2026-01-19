package me.bombom.api.v1.subscribe.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
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
    private static final Pattern ALREADY_UNSUBSCRIBED_PATTERN = Pattern.compile(
            "구독.?중인.?이메일.?주소가.?아닙니다|이미.?구독.?취소|이미.?취소|already.?unsubscribed|not.?subscribed|구독.?취소.?되었습니다",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ERROR_PATTERN = Pattern.compile(
            "error|오류|실패|failed|invalid|잘못|문제",
            Pattern.CASE_INSENSITIVE
    );

    public boolean unsubscribe(String url, Long newsletterId) {
        AtomicBoolean hasError = new AtomicBoolean(false);
        AtomicBoolean isProcessed = new AtomicBoolean(false);
        AtomicBoolean isReady = new AtomicBoolean(false);

        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new LaunchOptions()
                        .setHeadless(false)
                        .setArgs(List.of(
                                "--no-sandbox",
                                "--disable-setuid-sandbox",
                                "--disable-dev-shm-usage",
                                "--disable-gpu")))
        ) {
            BrowserContext context = getBrowserContext(browser);
            Page page = context.newPage();

            // 핸들러 등록
            setupDialogHandler(page, hasError, newsletterId);
            setupResponseHandler(page, hasError, isProcessed, isReady, newsletterId);

            page.navigate(url);

            // 이미 구독 취소 | URL 클릭 만으로 취소 성공
            if (isUnsubscribeSuccess(page)) {
                log.info("구독 취소 성공 (즉시 성공) - newsletterId: {}", newsletterId);
                return !hasError.get();
            }

            // 취소/확인 버튼 찾기 및 클릭
            Locator confirmButton = findUnsubscribeButton(page);
            if (confirmButton != null && confirmButton.isVisible()) {
                isProcessed.set(false);
                isReady.set(true);
                String beforeUrl = page.url();
                confirmButton.click();

                // 최대 3초간 응답 대기 (빠르게 오면 즉시 종료)
                long deadline = System.currentTimeMillis() + 3000;
                while (System.currentTimeMillis() < deadline) {
                    if (isProcessed.get() || hasError.get()) {
                        break;
                    }
                    page.waitForTimeout(500); // 0.5초 간격 확인
                }
                // 이미 구독 취소된 경우 종료
                if (isProcessed.get()) {
                    return true;
                }

                // 페이지 URL이 변경되었다면 성공으로 간주 (리다이렉트 발생)
                if (!page.url().equals(beforeUrl)) {
                    log.info("구독 취소 성공 (페이지 이동 감지) - newsletterId: {}", newsletterId);
                    return true;
                }

                // 에러가 없다면 성공으로 간주
                if (!hasError.get()) {
                    log.info("구독 취소 성공 (에러 없음) - newsletterId: {}", newsletterId);
                    return true;
                }

                return false;
            }

            log.error("구독 취소 실패: 버튼을 찾지 못함 - newsletterId: {}, URL: {}", newsletterId, url);
            return false;
        } catch (Exception e) {
            log.error("구독 취소 중 오류 발생 - newsletterId: {}, URL: {}, 오류: {}", newsletterId, url, e.getMessage(), e);
            return false;
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
        try {
            // 텍스트가 있는지 확인 (count > 0이면 존재)
            return page.getByText(SUCCESS_PATTERN).count() > 0;
        } catch (Exception e) {
            // 예외 발생 시 false 반환
            return false;
        }
    }

    private void setupDialogHandler(Page page, AtomicBoolean hasError, Long newsletterId) {
        page.onDialog(dialog -> {
            String message = dialog.message();
            if (ERROR_PATTERN.matcher(message).find()) {
                log.error("다이얼로그에서 에러 감지 - newsletterId: {}, 메시지: {}", newsletterId, message);
                hasError.set(true);
            }
            dialog.accept();
        });
    }

    private void setupResponseHandler(Page page, AtomicBoolean hasError, AtomicBoolean isProcessed,
            AtomicBoolean isReady,
            Long newsletterId) {
        page.onResponse(response -> {
            // 버튼 클릭 전에는 응답 처리하지 않음 (초기 페이지 로딩 로그 방지)
            if (!isReady.get()) {
                return;
            }

            int status = response.status();
            String url = response.url();
            if (isAdUrl(url)) {
                return;
            }

            // 200~300번대 응답도 성공으로 간주 (일반적인 성공 or 리다이렉트)
            if (status >= 200 && status < 400 && url.contains("unsubscribe")) {
                log.info("구독 취소 성공 응답 감지 (HTTP {}) - newsletterId: {}", status, newsletterId);
                isProcessed.set(true);
                return;
            }

            if (status >= 400 && status < 500 && url.contains("unsubscribe")) {
                String body = response.text();
                // "이미 구독 취소"는 성공으로 처리
                if (ALREADY_UNSUBSCRIBED_PATTERN.matcher(body).find()) {
                    log.info("이미 구독 취소됨 (HTTP {}) - newsletterId: {}", status, newsletterId);
                    isProcessed.set(true);
                } else {
                    log.error("HTTP {} 에러 - newsletterId: {}, URL: {}", status, newsletterId, url);
                    hasError.set(true);
                }
            }
        });
    }

    private boolean isAdUrl(String url) {
        return url.contains("google") || url.contains("doubleclick") || url.contains("adservice");
    }
}
