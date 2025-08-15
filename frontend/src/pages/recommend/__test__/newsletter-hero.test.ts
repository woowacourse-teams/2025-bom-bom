import { test, expect } from '@playwright/test';

test.describe('추천 페이지 - 뉴스레터 히어로 섹션', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/recommend');
  });

  test('비로그인 사용자에게 히어로 섹션이 표시되어야 한다', async ({
    page,
  }) => {
    await expect(page.getByText('🌸')).toBeVisible();
    await expect(
      page.getByText('새로운 뉴스레터를 발견해보세요! 📚'),
    ).toBeVisible();
    await expect(
      page.getByText('당신의 관심사에 맞는 최고의 뉴스레터를 추천해드립니다.'),
    ).toBeVisible();
  });

  test('CTA 버튼이 표시되고 로그인 페이지로 연결되어야 한다', async ({
    page,
  }) => {
    const ctaButton = page.getByText('로그인하고 맞춤 추천 받기');
    await expect(ctaButton).toBeVisible();
    await expect(ctaButton).toHaveAttribute('href', '/login');
    await ctaButton.click();
    await expect(page).toHaveURL('/login');
  });

  test('올바른 스타일과 레이아웃을 가져야 한다', async ({ page }) => {
    const heroContent = page
      .locator('div')
      .filter({ hasText: /새로운 뉴스레터를 발견해보세요/ })
      .first();
    await expect(heroContent).toBeVisible();
    const ctaButton = page.getByText('로그인하고 맞춤 추천 받기');
    await expect(ctaButton).toBeEnabled();
  });

  test('그라데이션 배경이 표시되어야 한다', async ({ page }) => {
    const heroContent = page
      .locator('div')
      .filter({ hasText: /새로운 뉴스레터를 발견해보세요/ })
      .first();
    await expect(heroContent).toBeVisible();
  });

  test('모바일에서 반응형으로 동작해야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.getByText('🌸')).toBeVisible();
    await expect(
      page.getByText('새로운 뉴스레터를 발견해보세요! 📚'),
    ).toBeVisible();
    await expect(page.getByText('로그인하고 맞춤 추천 받기')).toBeVisible();
  });
});
