import { test, expect } from '@playwright/test';

test.describe('추천 페이지 - 전체 레이아웃 및 통합', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/recommend');
  });

  test('페이지가 성공적으로 로드되어야 한다', async ({ page }) => {
    await expect(page).toHaveTitle('봄봄');
    await expect(page).toHaveURL('/recommend');
  });

  test('모든 주요 섹션이 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('봄봄').first()).toBeVisible();
    await expect(page.getByText('뉴스레터 추천')).toBeVisible();

    await expect(
      page.getByText('새로운 뉴스레터를 발견해보세요! 📚'),
    ).toBeVisible();

    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    await expect(page.getByText('이달의 독서왕')).toBeVisible();
  });

  test('올바른 레이아웃 구조를 가져야 한다', async ({ page }) => {
    const mainContainer = page.locator('div').first();
    await expect(mainContainer).toBeVisible();

    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();
  });

  test('반응형이고 모바일 친화적이어야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();

    await page.setViewportSize({ width: 768, height: 1024 });
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();

    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
    await expect(page.getByText('이달의 독서왕')).toBeVisible();
  });

  test('페이지 스크롤을 올바르게 처리해야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 600 });

    await page.keyboard.press('End');

    await expect(page.getByText('이달의 독서왕')).toBeVisible();

    await page.keyboard.press('Home');
    await expect(page.getByText('봄봄').first()).toBeVisible();
  });

  test('올바른 메타 정보가 표시되어야 한다', async ({ page }) => {
    await expect(page).toHaveTitle('봄봄');

    const viewport = await page.getAttribute(
      'meta[name="viewport"]',
      'content',
    );
    expect(viewport).toBeTruthy();
  });

  test('모든 이미지가 성공적으로 로드되어야 한다', async ({ page }) => {
    const images = page.locator('img');
    const imageCount = await images.count();

    if (imageCount > 0) {
      const firstImage = images.first();
      await expect(firstImage).toBeVisible();

      const isLoaded = await firstImage.evaluate((img: HTMLImageElement) => {
        return img.complete && img.naturalHeight !== 0;
      });

      expect(isLoaded).toBeTruthy();
    }
  });

  test('성능 기준을 유지해야 한다', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/recommend');
    const loadTime = Date.now() - startTime;

    expect(loadTime).toBeLessThan(10000);

    await expect(
      page.getByText('새로운 뉴스레터를 발견해보세요! 📚'),
    ).toBeVisible();
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
  });
});
