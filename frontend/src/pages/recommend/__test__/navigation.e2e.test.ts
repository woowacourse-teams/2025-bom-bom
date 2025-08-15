import { test, expect } from '@playwright/test';

test.describe('Recommend Page - Navigation', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/recommend');
  });

  test('should display navigation menu correctly', async ({ page }) => {
    // 네비게이션 메뉴가 표시되는지 확인
    const navigation = page.locator('nav');
    await expect(navigation).toBeVisible();

    // 각 네비게이션 링크가 표시되는지 확인
    await expect(page.getByText('오늘의 뉴스레터')).toBeVisible();
    await expect(page.getByText('뉴스레터 보관함')).toBeVisible();
    await expect(page.getByText('뉴스레터 추천')).toBeVisible();
  });

  test('should navigate to correct pages when clicking navigation links', async ({
    page,
  }) => {
    // "오늘의 뉴스레터" 링크 클릭
    await page.getByText('오늘의 뉴스레터').click();
    await expect(page).toHaveURL('http://localhost:3000/');

    // 다시 추천 페이지로 이동
    await page.goto('http://localhost:3000/recommend');

    // "뉴스레터 보관함" 링크 클릭
    await page.getByText('뉴스레터 보관함').click();
    await expect(page).toHaveURL('http://localhost:3000/storage');

    // 다시 추천 페이지로 이동
    await page.goto('http://localhost:3000/recommend');

    // "뉴스레터 추천" 링크는 현재 페이지이므로 클릭해도 같은 페이지
    await page.getByText('뉴스레터 추천').click();
    await expect(page).toHaveURL('http://localhost:3000/recommend');
  });

  test('should display login button', async ({ page }) => {
    // 로그인 버튼이 표시되는지 확인
    const loginButton = page.getByText('로그인');
    await expect(loginButton).toBeVisible();
    await expect(loginButton).toHaveAttribute('role', 'button');
  });

  test('should display brand logo and navigate to home', async ({ page }) => {
    // 브랜드 로고가 표시되는지 확인
    const brandLogo = page.getByText('봄봄').first();
    await expect(brandLogo).toBeVisible();

    // 로고 클릭시 홈으로 이동
    await brandLogo.click();
    await expect(page).toHaveURL('http://localhost:3000/');
  });
});
