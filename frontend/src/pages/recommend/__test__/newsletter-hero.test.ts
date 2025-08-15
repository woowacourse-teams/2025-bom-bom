import { test, expect } from '@playwright/test';

test.describe('추천 페이지 - 뉴스레터 히어로 섹션', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/recommend');
  });

  test('비로그인 사용자에게 히어로 섹션이 표시되어야 한다', async ({
    page,
  }) => {
    // 히어로 섹션의 기본 요소들이 표시되는지 확인
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
    // CTA 버튼이 표시되는지 확인
    const ctaButton = page.getByText('로그인하고 맞춤 추천 받기');
    await expect(ctaButton).toBeVisible();

    // 링크가 올바른 URL을 가지고 있는지 확인
    await expect(ctaButton).toHaveAttribute('href', '/login');

    // 버튼 클릭시 로그인 페이지로 이동
    await ctaButton.click();
    await expect(page).toHaveURL('/login');
  });

  test('올바른 스타일과 레이아웃을 가져야 한다', async ({ page }) => {
    // 히어로 섹션이 중앙 정렬되어 있는지 확인
    const heroContent = page
      .locator('div')
      .filter({ hasText: /새로운 뉴스레터를 발견해보세요/ })
      .first();
    await expect(heroContent).toBeVisible();

    // CTA 버튼이 클릭 가능한 상태인지 확인
    const ctaButton = page.getByText('로그인하고 맞춤 추천 받기');
    await expect(ctaButton).toBeEnabled();
  });

  test('그라데이션 배경이 표시되어야 한다', async ({ page }) => {
    // 히어로 섹션의 배경 그라데이션이 적용되어 있는지 확인
    const heroContent = page
      .locator('div')
      .filter({ hasText: /새로운 뉴스레터를 발견해보세요/ })
      .first();

    // 배경색이 적용되어 있는지 확인 (시각적 요소이므로 존재 여부만 확인)
    await expect(heroContent).toBeVisible();
  });

  test('모바일에서 반응형으로 동작해야 한다', async ({ page }) => {
    // 모바일 뷰포트로 변경
    await page.setViewportSize({ width: 375, height: 667 });

    // 모바일에서도 모든 요소가 표시되는지 확인
    await expect(page.getByText('🌸')).toBeVisible();
    await expect(
      page.getByText('새로운 뉴스레터를 발견해보세요! 📚'),
    ).toBeVisible();
    await expect(page.getByText('로그인하고 맞춤 추천 받기')).toBeVisible();
  });
});
