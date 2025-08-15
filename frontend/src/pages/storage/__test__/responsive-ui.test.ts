import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 반응형 UI 테스트', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    const currentUrl = page.url();
    if (currentUrl.includes('/recommend') || currentUrl.includes('/login')) {
      await page.goto('/storage');
      await page.waitForLoadState('networkidle');
    }
  });

  test('데스크톱에서 사이드바가 표시되어야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '뉴스레터', level: 3 }),
    ).toBeVisible();
    await expect(
      page.getByRole('heading', { name: '바로 가기', level: 3 }),
    ).toBeVisible();

    await expect(page.getByRole('link', { name: '북마크' })).toBeVisible();
    await expect(page.getByRole('link', { name: '메모' })).toBeVisible();
  });

  test('모바일에서 사이드바가 숨겨져야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '뉴스레터', level: 3 }),
    ).not.toBeVisible();
    await expect(
      page.getByRole('heading', { name: '바로 가기', level: 3 }),
    ).not.toBeVisible();

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
  });

  test('데스크톱에서 풀 네비게이션이 표시되어야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('link', { name: '오늘의 뉴스레터' }),
    ).toBeVisible();
    await expect(
      page.getByRole('link', { name: '뉴스레터 보관함' }),
    ).toBeVisible();
    await expect(
      page.getByRole('link', { name: '뉴스레터 추천' }),
    ).toBeVisible();

    await expect(page.getByText('봄봄')).toBeVisible();
    await expect(
      page.getByText('당신의 하루에 찾아오는 작은 설렘'),
    ).toBeVisible();
  });

  test('모바일에서 간소화된 헤더가 표시되어야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');

    await expect(page.getByText('봄봄')).toBeVisible();

    await expect(page.getByRole('button', { name: '로그인' })).toBeVisible();

    const navigationLinks = page.getByRole('navigation').getByRole('link');
    await expect(navigationLinks.first()).toBeVisible();
  });

  test('모바일에서 컨텐츠 레이아웃이 세로로 배치되어야 한다', async ({
    page,
  }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');

    await expect(page.getByText('전체')).toBeVisible();
    await expect(page.getByText('테크뉴스')).toBeVisible();

    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
    await expect(
      page.locator('p').filter({ hasText: '최신순' }).first(),
    ).toBeVisible();

    await expect(page.getByRole('list')).toBeVisible();
  });

  test('태블릿 크기에서도 적절히 반응해야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
    await expect(page.getByRole('list')).toBeVisible();

    await expect(
      page.getByRole('link', { name: '오늘의 뉴스레터' }),
    ).toBeVisible();
  });

  test('화면 크기 변경 시 즉시 반응해야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '바로 가기', level: 3 }),
    ).toBeVisible();

    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForTimeout(500);

    await expect(
      page.getByRole('heading', { name: '바로 가기', level: 3 }),
    ).not.toBeVisible();

    await page.setViewportSize({ width: 1280, height: 720 });
    await page.waitForTimeout(500);

    await expect(
      page.getByRole('heading', { name: '바로 가기', level: 3 }),
    ).toBeVisible();
  });

  test('모든 화면 크기에서 기본 기능이 작동해야 한다', async ({ page }) => {
    const viewports = [
      { width: 320, height: 568 },
      { width: 375, height: 667 },
      { width: 768, height: 1024 },
      { width: 1024, height: 768 },
      { width: 1280, height: 720 },
    ];

    for (const viewport of viewports) {
      await page.setViewportSize(viewport);
      await page.waitForLoadState('networkidle');

      await expect(
        page.getByRole('heading', { name: '뉴스레터 보관함' }),
      ).toBeVisible();
      await expect(page.getByText('전체')).toBeVisible();
      await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
      await expect(
        page.locator('p').filter({ hasText: '최신순' }).first(),
      ).toBeVisible();

      const searchBox = page.getByRole('searchbox', { name: '검색' });
      await searchBox.fill('테스트');
      await expect(searchBox).toHaveValue('테스트');
      await searchBox.clear();
    }
  });

  test('터치 디바이스에서 탭 기능이 작동해야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForLoadState('networkidle');

    await page.getByText('테크뉴스').click();

    await page.waitForLoadState('networkidle');

    const searchBox = page.getByRole('searchbox', { name: '검색' });
    await searchBox.click();
    await searchBox.fill('모바일 테스트');
    await expect(searchBox).toHaveValue('모바일 테스트');
  });
});
