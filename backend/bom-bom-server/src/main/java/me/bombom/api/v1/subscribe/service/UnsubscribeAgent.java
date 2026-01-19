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
            setupResponseHandler(page, hasError, isProcessed, newsletterId);

            page.navigate(url);

            // 이미 구독 취소 | URL 클릭 만으로 취소 성공
            if (isUnsubscribeSuccess(page)) {
                log.info("구독 취소 성공 (즉시 성공) - newsletterId: {}", newsletterId);
                return !hasError.get();
            }

            // 취소/확인 버튼 찾기 및 클릭
            Locator confirmButton = findUnsubscribeButton(page);
            if (confirmButton != null && confirmButton.isVisible()) {
                confirmButton.click();
                page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(3000));

                // 이미 구독 취소된 경우 종료
                if (isProcessed.get()) {
                    return true;
                }

                // 버튼 클릭 후 성공 여부 확인
                if (isUnsubscribeSuccess(page) || isAlreadyUnsubscribed(page)) {
                    log.info("구독 취소 성공 (버튼 클릭) - newsletterId: {}", newsletterId);
                    return true;
                }

                log.info("구독 취소 성공 추정 (버튼 클릭 완료) - newsletterId: {}", newsletterId);
                return !hasError.get();
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

    private boolean isAlreadyUnsubscribed(Page page) {
        try {
            // 텍스트가 있는지 확인 (count > 0이면 존재)
            return page.getByText(ALREADY_UNSUBSCRIBED_PATTERN).count() > 0;
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

    private void setupResponseHandler(Page page, AtomicBoolean hasError, AtomicBoolean isProcessed, Long newsletterId) {
        page.onResponse(response -> {
            int status = response.status();
            String url = response.url();
            // 광고성 URL은 무시
            if (url.contains("google") || url.contains("doubleclick") || url.contains("adservice")) {
                return;
            }
            if (status >= 400 && status < 500 && url.contains("unsubscribe")) {
                String body = response.text();

                // "이미 구독 취소"는 성공으로 처리 (hasError 미설정)
                if (ALREADY_UNSUBSCRIBED_PATTERN.matcher(body).find()) {
                    log.info("이미 구독 취소됨 (HTTP {}) - newsletterId: {}", status, newsletterId);
                    isProcessed.set(true); // 처리 완료 표시
                } else {
                    log.error("HTTP {} 에러 - newsletterId: {}, URL: {}", status, newsletterId, url);
                    hasError.set(true);
                }
            }
        });
    }
}
