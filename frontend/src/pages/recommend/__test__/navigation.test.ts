import { test, expect } from '@playwright/test';

test.describe('추천 페이지 - 네비게이션', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/recommend');
  });

  test('네비게이션 메뉴가 올바르게 표시되어야 한다', async ({ page }) => {
    const navigation = page.locator('nav');
    await expect(navigation).toBeVisible();
    await expect(page.getByText('오늘의 뉴스레터')).toBeVisible();
    await expect(page.getByText('뉴스레터 보관함')).toBeVisible();
    await expect(page.getByText('뉴스레터 추천')).toBeVisible();
  });

  test('네비게이션 링크를 클릭하면 올바른 페이지로 이동해야 한다', async ({
    page,
  }) => {
    await page.getByText('오늘의 뉴스레터').click();
    await expect(page).toHaveURL('/');
    await page.goto('/recommend');
    await page.getByText('뉴스레터 보관함').click();
    await expect(page).toHaveURL('/storage');
    await page.goto('/recommend');
    await page.getByText('뉴스레터 추천').click();
    await expect(page).toHaveURL('/recommend');
  });

  test('로그인을 하지 않았을 때 로그인 버튼이 표시되어야 한다', async ({
    page,
  }) => {
    const loginButton = page.getByRole('button', { name: '로그인' });
    await expect(loginButton).toBeVisible();
  });

  test('브랜드 로고가 표시되고 홈으로 이동해야 한다', async ({ page }) => {
    const brandLogo = page.getByText('봄봄').first();
    await expect(brandLogo).toBeVisible();
    await brandLogo.click();
    await expect(page).toHaveURL('/');
  });
});
