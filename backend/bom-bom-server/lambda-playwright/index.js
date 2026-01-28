/**
 * AWS Lambda - Playwright 뉴스레터 구독 자동 해지 엔진
 * 
 * 이 엔진은 기존 Spring Boot의 UnsubscribeAgent 로직을 람다 환경으로 이식한 것입니다.
 * 단순한 클릭을 넘어 네트워크 응답 분석, 다이얼로그 처리, 리다이렉트 감지 등 
 * 다각적인 성공 판정 로직을 포함하고 있습니다.
 */

const chromium = require('@sparticuz/chromium');
const { chromium: playwright } = require('playwright-core');

// [상수] 브라우저 식별자 (User-Agent)
const USER_AGENT = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36';

// [상수] 차단할 리소스 유형 (Java의 BLOCKED_RESOURCE_TYPES와 동일)
const BLOCKED_RESOURCE_TYPES = ['image', 'font', 'media'];

// [상수] 대기 시간 및 타임아웃
const UNSUBSCRIBE_TIMEOUT_MS = 10000; // 10초
const NAVIGATION_TIMEOUT_MS = 30000;    // 30초
const POLLING_INTERVAL_MS = 500;       // 0.5초

exports.handler = async (event) => {
    const startTime = Date.now();
    console.log('🚀 [START] Lambda 실행 시작');
    console.log('📦 [EVENT] 데이터:', JSON.stringify(event));

    // 메모리 상태 로그
    const logMemory = (tag = 'Status') => {
        const used = process.memoryUsage();
        console.log(`📊 [MEMORY ${tag}]: rss=${Math.round(used.rss / 1024 / 1024)}MB, heapUsed=${Math.round(used.heapUsed / 1024 / 1024)}MB`);
    };
    logMemory('Initial');

    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event;
    const { url, patterns } = body;

    if (!url) {
        console.error('❌ [ERROR] 해지 URL이 누락되었습니다.');
        return { success: false, statusCode: 400, message: '해지 URL이 누락되었습니다.' };
    }

    const regex = {
        unsubscribe: new RegExp(patterns?.unsubscribe || 'unsubscribe|구독.?취소|해지|수신.?거부', 'i'),
        success: new RegExp(patterns?.success || 'success|unsubscribed|완료|되었습니다|성공', 'i'),
        alreadyUnsubscribed: new RegExp(patterns?.alreadyUnsubscribed || 'already|이미|하고 계시지 않습니다|수신.?거부', 'i'),
        error: new RegExp(patterns?.error || 'error|failed|실패|오류', 'i')
    };

    const adDomains = patterns?.adDomains || ['doubleclick.net', 'google-analytics.com', 'googletagmanager.com'];

    let browser = null;
    let isProcessed = false;
    let hasError = false;
    let isReady = false;

    try {
        console.log('🌐 [STEP 1] 브라우저 실행 준비 중...');
        browser = await playwright.launch({
            args: [...chromium.args, '--disable-gpu', '--disable-dev-shm-usage'],
            executablePath: await chromium.executablePath(),
            headless: true,
        });
        console.log('✅ [STEP 1] 브라우저 실행 완료');
        logMemory('BrowserUp');

        const context = await browser.newContext({
            userAgent: USER_AGENT,
            viewport: { width: 1280, height: 720 },
            extraHTTPHeaders: {
                'Accept-Language': 'ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7',
                'Referer': 'https://www.google.com/'
            },
            locale: 'ko-KR',
            timezoneId: 'Asia/Seoul'
        });

        await context.route('**/*', (route) => {
            try {
                const type = route.request().resourceType();
                if (BLOCKED_RESOURCE_TYPES.includes(type)) {
                    route.abort();
                } else {
                    route.continue();
                }
            } catch (e) {
                route.continue();
            }
        });

        const page = await context.newPage();

        page.on('dialog', async (dialog) => {
            try {
                const message = dialog.message();
                console.log(`💬 [DIALOG 감지]: ${message}`);

                if (regex.error.test(message)) {
                    console.log('⚠️ [DIALOG 에러 감지]: 에러 관련 문구가 포함되어 있습니다.');
                    hasError = true;
                }

                if (regex.success.test(message) || regex.alreadyUnsubscribed.test(message)) {
                    console.log('✨ [DIALOG 성공 감지]: 성공 문구가 포착되었습니다.');
                    isProcessed = true;
                }

                await dialog.accept();
            } catch (e) {
                console.error('⚠️ [DIALOG 오류]:', e);
            }
        });

        page.on('response', async (response) => {
            try {
                if (!isReady) return;

                const resUrl = response.url();
                const status = response.status();

                if (adDomains.some(domain => resUrl.includes(domain))) return;

                if (status >= 200 && status < 400 && resUrl.toLowerCase().includes('unsubscribe')) {
                    console.log(`📡 [XHR 성공 감지]: ${resUrl.substring(0, 60)}... (${status})`);
                    isProcessed = true;
                }

                if (status >= 400 && status < 500 && resUrl.toLowerCase().includes('unsubscribe')) {
                    const resBody = await response.text().catch(() => '');
                    if (regex.alreadyUnsubscribed.test(resBody)) {
                        console.log(`📡 [XHR 이미해지 감지]: ${status}`);
                        isProcessed = true;
                    } else {
                        console.log(`📡 [XHR 에러 감지]: ${resUrl.substring(0, 60)}... (${status})`);
                        hasError = true;
                    }
                }
            } catch (e) {
                console.warn('⚠️ [RESPONSE 오류]:', e.message);
            }
        });

        console.log(`🔗 [STEP 2] 페이지 접속 시도: ${url}`);
        // networkidle: 최소 500ms 동안 네트워크 통신이 없을 때까지 대기 (SPA에 유효)
        await page.goto(url, { waitUntil: 'networkidle', timeout: NAVIGATION_TIMEOUT_MS }).catch(() =>
            page.goto(url, { waitUntil: 'load', timeout: NAVIGATION_TIMEOUT_MS }) // 실패 시 load로 재시도
        );

        const pageTitle = await page.title().catch(() => 'No Title');
        console.log(`✅ [STEP 2] 페이지 로드 완료 (Title: "${pageTitle}")`);

        // 사용자의 피드백을 반영하여 대기 시간을 충분히 확보 (5초)
        console.log('⏳ 페이지 안정화 및 동적 콘텐츠 대기 중 (5s)...');
        await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight)).catch(() => { });
        await page.waitForTimeout(5000);
        logMemory('PageLoaded');

        const checkSuccessText = async (contextName = 'auto') => {
            try {
                const frames = page.frames();
                for (const frame of frames) {
                    const text = await frame.innerText('body').catch(() => '');
                    const altText = text.trim() ? text : await frame.textContent('body').catch(() => '');

                    const isSuccess = regex.success.test(altText) || regex.alreadyUnsubscribed.test(altText);

                    if (isSuccess) {
                        // 코드가 아닌 '진짜 문구'만 남김 (window, const, { } 등 포함된 복잡한 코드는 모두 제거)
                        const cleanText = altText
                            .replace(/[a-zA-Z0-9_$]+\.[a-zA-Z0-9_$]+ *=.*/g, '')
                            .replace(/(const|let|var) .*/g, '')
                            .replace(/{[\s\S]*?}/g, '')
                            .replace(/\s+/g, ' ')
                            .trim()
                            .substring(0, 100);

                        if (cleanText.length > 5) {
                            console.log(`✨ [TEXT 성공 감지 (${contextName})]: "${cleanText}..."`);
                        }
                        return true;
                    }
                }
            } catch (e) {
                console.log(`⚠️ [TEXT 확인 중 오류 - ${contextName}]:`, e.message);
            }
            return false;
        };

        if (await checkSuccessText('initial')) {
            console.log('🎯 [RESULT] 접속 즉시 성공 확인됨');
            return { success: true, statusCode: 200, message: '페이지 접속 직후 성공 문구가 발견되었습니다.', method: 'INSTANT_DETECTION' };
        }

        isReady = true;
        const beforeUrl = page.url();

        console.log('🔍 [STEP 3] 해지 버튼 검색 중...');
        const buttons = page.locator('button:visible, a:visible, input[type="button"]:visible, input[type="submit"]:visible');
        const count = await buttons.count();
        let targetButton = null;

        console.log(`🔘 발견된 가시적 버튼 수: ${count}`);

        for (let i = 0; i < count; i++) {
            const btn = buttons.nth(i);
            const [text, value] = await Promise.all([
                btn.innerText().catch(() => ''),
                btn.getAttribute('value').catch(() => '')
            ]);

            if (regex.unsubscribe.test(text) || regex.unsubscribe.test(value)) {
                targetButton = btn;
                console.log(`🎯 타겟 버튼 발견: "${(text.trim() || value).substring(0, 20)}"`);
                break;
            }
        }

        if (targetButton) {
            console.log('🖱️ [STEP 4] 해지 버튼 클릭!');
            await targetButton.click();

            const deadline = Date.now() + UNSUBSCRIBE_TIMEOUT_MS;
            while (Date.now() < deadline) {
                if (isProcessed) {
                    console.log('🎯 [RESULT] 네트워크 응답(XHR)으로 성공 확인');
                    return { success: true, statusCode: 200, message: '백그라운드 네트워크 통신을 통해 해지 승인을 확인했습니다.', method: 'NETWORK_CONFIRMATION' };
                }
                if (hasError) break;

                if (page.url() !== beforeUrl) {
                    console.log(`🔄 [REDIRECT] URL 변경 감지: ${page.url().substring(0, 60)}...`);
                    await page.waitForTimeout(1000);
                    if (await checkSuccessText('after_navigation')) {
                        console.log('🎯 [RESULT] 페이지 이동 후 문구로 성공 확인');
                        return { success: true, statusCode: 200, message: '페이지 이동 후 화면에서 성공 문구가 포착되었습니다.', method: 'NAVIGATION_SUCCESS' };
                    }
                    console.log('🎯 [RESULT] URL 변경으로 성공 간주');
                    return { success: true, statusCode: 200, message: '해지 프로세스 완료 후 페이지가 이동되어 성공한 것으로 간주합니다.', method: 'NAVIGATION_SUCCESS' };
                }

                if (await checkSuccessText('polling')) {
                    console.log('🎯 [RESULT] 화면 문구 폴링으로 성공 확인');
                    return { success: true, statusCode: 200, message: '클릭 후 화면에 성공 문구가 나타난 것을 확인했습니다.', method: 'SCREEN_TEXT_MATCH' };
                }

                await page.waitForTimeout(POLLING_INTERVAL_MS);
            }
        }

        if (isProcessed) {
            console.log('🎯 [RESULT] 최종 응답(XHR)으로 성공 확인');
            return { success: true, statusCode: 200, message: '최종 네트워크 응답 결과가 성공으로 판정되었습니다.', method: 'NETWORK_CONFIRMATION' };
        }

        if (await checkSuccessText('final')) {
            console.log('🎯 [RESULT] 최종 화면 문구로 성공 확인');
            return { success: true, statusCode: 200, message: '최종적으로 화면에서 성공 문구가 발견되었습니다.', method: 'SCREEN_TEXT_MATCH' };
        }

        console.warn('🛑 [RESULT] 성공 결과를 확인하지 못함');
        const finalUrl = page.url();
        const html = await page.content().catch(() => '');
        const finalBody = await page.textContent('body').catch(() => '');

        console.log(`🔗 [DEBUG] 최종 URL: ${finalUrl}`);
        console.log(`📄 [DEBUG] HTML 길이: ${html.length}`);

        // 코드가 아닌 '진짜 본문'만 추출 (JSON이나 JS 변수 선언 등은 로그에서 제외)
        const cleanBody = finalBody
            .replace(/[a-zA-Z0-9_$]+\.[a-zA-Z0-9_$]+ *=.*/g, '')
            .replace(/(const|let|var) .*/g, '')
            .replace(/{[\s\S]*?}/g, '')
            .replace(/\s+/g, ' ')
            .trim()
            .substring(0, 300);

        console.log(`🔍 [DEBUG] 최종 본문 텍스트: "${cleanBody}..."`);

        return {
            success: false,
            statusCode: hasError ? 500 : 404,
            message: hasError ? '사이트에서 실패 관련 팝업 혹은 에러가 감지되었습니다.' : '정해진 시간 내에 성공 결과를 확인하지 못했습니다.'
        };

    } catch (error) {
        console.error('💥 [CRITICAL] 람다 내부 예외 발생:', error);
        return { success: false, statusCode: 500, message: `람다 런타임 오류: ${error.message}` };
    } finally {
        const duration = Date.now() - startTime;
        console.log(`🏁 [FINISH] 실행 종료 (소요 시간: ${duration}ms)`);
        logMemory('Finished');
        if (browser) await browser.close().catch(() => { });
    }
};
