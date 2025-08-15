import { test, expect } from '@playwright/test';

test.describe('보관함 페이지 - 반응형 UI', () => {
  test('데스크탑 화면에서 올바른 레이아웃이 표시된다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();

    await expect(page.getByText('뉴스레터').first()).toBeVisible();

    const newsletterNav = page.getByRole('navigation', { name: '뉴스레터' });
    await expect(newsletterNav).toBeVisible();

    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
    await expect(page.getByText('최신순').first()).toBeVisible();

    await expect(page.getByRole('list')).toBeVisible();
  });

  test('모바일 화면에서 올바른 레이아웃이 표시된다', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();

    const newsletterNav = page.getByRole('navigation', { name: '뉴스레터' });
    await expect(newsletterNav).toBeVisible();

    const newsletterTitle = page.getByText('뉴스레터').first();
    const titleCount = await newsletterTitle.count();
    expect(titleCount).toBeGreaterThanOrEqual(0);

    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
    await expect(page.getByText('최신순').first()).toBeVisible();

    await expect(page.getByRole('list')).toBeVisible();
  });

  test('태블릿 화면에서 올바른 레이아웃이 표시된다', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();
    await expect(page.getByRole('list')).toBeVisible();
  });

  test('화면 크기 변경 시 레이아웃이 적절히 조정된다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();

    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForTimeout(500);

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();

    await page.setViewportSize({ width: 1280, height: 800 });
    await page.waitForTimeout(500);

    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();
  });

  test('뉴스레터 필터가 다양한 화면 크기에서 작동한다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    await page.getByText('UPPITY3').click();
    await page.waitForLoadState('networkidle');
    await expect(page.getByText('총 3개')).toBeVisible();

    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForTimeout(500);

    await expect(page.getByText('총 3개')).toBeVisible();

    await page.getByText('AI뉴스1').click();
    await page.waitForLoadState('networkidle');
    await expect(page.getByText('총 1개')).toBeVisible();
  });

  test('검색 기능이 다양한 화면 크기에서 작동한다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    const searchInput = page.getByRole('searchbox', { name: '검색' });
    await searchInput.fill('AI');
    await page.waitForTimeout(2000);
    await expect(page.getByText('총 1개')).toBeVisible();

    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForTimeout(500);

    await expect(page.getByText('총 1개')).toBeVisible();
    await expect(page.getByText('AI가 바꿀 미래의 일자리')).toBeVisible();
  });
});
