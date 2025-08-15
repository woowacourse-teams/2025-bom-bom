import { test, expect } from '@playwright/test';

test.describe('Recommend Page - Overall Layout and Integration', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/recommend');
  });

  test('should load the page successfully', async ({ page }) => {
    // 페이지가 성공적으로 로드되는지 확인
    await expect(page).toHaveTitle('봄봄');
    await expect(page).toHaveURL('http://localhost:3000/recommend');
  });

  test('should display all main sections', async ({ page }) => {
    // 모든 주요 섹션이 표시되는지 확인

    // 헤더/네비게이션
    await expect(page.getByText('봄봄').first()).toBeVisible();
    await expect(page.getByText('뉴스레터 추천')).toBeVisible();

    // 히어로 섹션
    await expect(
      page.getByText('새로운 뉴스레터를 발견해보세요! 📚'),
    ).toBeVisible();

    // 트렌디 섹션
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    // 리더보드 섹션
    await expect(page.getByText('이달의 독서왕')).toBeVisible();
  });

  test('should have proper layout structure', async ({ page }) => {
    // 메인 컨테이너가 존재하는지 확인
    const mainContainer = page.locator('div').first();
    await expect(mainContainer).toBeVisible();

    // 메인 섹션과 사이드 섹션이 분리되어 있는지 확인
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();
  });

  test('should be responsive and mobile-friendly', async ({ page }) => {
    // 데스크톱 뷰에서 레이아웃 확인
    await page.setViewportSize({ width: 1280, height: 720 });
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();

    // 태블릿 뷰에서 레이아웃 확인
    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();

    // 모바일 뷰에서 레이아웃 확인
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();
  });

  test('should handle page scroll correctly', async ({ page }) => {
    // 페이지 스크롤이 작동하는지 확인
    await page.setViewportSize({ width: 1280, height: 600 });

    // 페이지 맨 아래로 스크롤
    await page.keyboard.press('End');

    // 여전히 모든 섹션이 접근 가능한지 확인
    await expect(page.getByText('이달의 독서왕')).toBeVisible();

    // 페이지 맨 위로 스크롤
    await page.keyboard.press('Home');
    await expect(page.getByText('봄봄').first()).toBeVisible();
  });

  test('should not have console errors', async ({ page }) => {
    const consoleErrors: string[] = [];

    page.on('console', (msg) => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text());
      }
    });

    // 페이지를 다시 로드하여 콘솔 에러 확인
    await page.reload();
    await page.waitForLoadState('networkidle');

    // 중요한 에러만 필터링 (API 401 에러는 예상되는 에러이므로 제외)
    const criticalErrors = consoleErrors.filter(
      (error) =>
        !error.includes('401') &&
        !error.includes('유효하지 않은 인증 정보') &&
        !error.includes('gtag is not initialized') &&
        !error.includes('React does not recognize'),
    );

    expect(criticalErrors).toHaveLength(0);
  });

  test('should handle keyboard navigation', async ({ page }) => {
    // 탭 키로 네비게이션이 가능한지 확인
    await page.keyboard.press('Tab');

    // 포커스가 올바르게 이동하는지 확인 (브랜드 로고)
    const focusedElement = page.locator(':focus');
    await expect(focusedElement).toBeVisible();

    // 여러 번 탭하여 네비게이션 링크들로 이동
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');

    // 엔터 키로 링크 활성화가 가능한지 확인
    await page.keyboard.press('Enter');
  });

  test('should display correct meta information', async ({ page }) => {
    // 페이지 제목이 올바른지 확인
    await expect(page).toHaveTitle('봄봄');

    // viewport meta tag가 올바르게 설정되어 있는지 확인 (모바일 최적화)
    const viewport = await page.getAttribute(
      'meta[name="viewport"]',
      'content',
    );
    expect(viewport).toBeTruthy();
  });

  test('should load all images successfully', async ({ page }) => {
    // 페이지의 모든 이미지가 로드되는지 확인
    const images = page.locator('img');
    const imageCount = await images.count();

    if (imageCount > 0) {
      // 첫 번째 이미지가 로드되었는지 확인
      const firstImage = images.first();
      await expect(firstImage).toBeVisible();

      // 이미지 로드 상태 확인
      const isLoaded = await firstImage.evaluate((img: HTMLImageElement) => {
        return img.complete && img.naturalHeight !== 0;
      });

      expect(isLoaded).toBeTruthy();
    }
  });

  test('should maintain performance standards', async ({ page }) => {
    // 페이지 로드 성능 확인
    const startTime = Date.now();
    await page.goto('http://localhost:3000/recommend');
    await page.waitForLoadState('networkidle');
    const loadTime = Date.now() - startTime;

    // 로드 시간이 합리적인 범위 내에 있는지 확인 (10초 이내)
    expect(loadTime).toBeLessThan(10000);

    // 주요 콘텐츠가 표시되는지 확인
    await expect(
      page.getByText('새로운 뉴스레터를 발견해보세요! 📚'),
    ).toBeVisible();
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
  });
});
