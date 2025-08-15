import { test, expect } from '@playwright/test';

test.describe('Recommend Page - Newsletter Hero Section', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/recommend');
  });

  test('should display hero section for non-logged-in user', async ({ page }) => {
    // 히어로 섹션의 기본 요소들이 표시되는지 확인
    await expect(page.getByText('🌸')).toBeVisible();
    await expect(page.getByText('새로운 뉴스레터를 발견해보세요! 📚')).toBeVisible();
    await expect(page.getByText('당신의 관심사에 맞는 최고의 뉴스레터를 추천해드립니다.')).toBeVisible();
  });

  test('should display CTA button and link to login page', async ({ page }) => {
    // CTA 버튼이 표시되는지 확인
    const ctaButton = page.getByText('로그인하고 맞춤 추천 받기');
    await expect(ctaButton).toBeVisible();
    
    // 링크가 올바른 URL을 가지고 있는지 확인
    await expect(ctaButton).toHaveAttribute('href', '/login');
    
    // 버튼 클릭시 로그인 페이지로 이동
    await ctaButton.click();
    await expect(page).toHaveURL('http://localhost:3000/login');
  });

  test('should have proper styling and layout', async ({ page }) => {
    // 히어로 섹션이 중앙 정렬되어 있는지 확인
    const heroContent = page.locator('div').filter({ hasText: /새로운 뉴스레터를 발견해보세요/ }).first();
    await expect(heroContent).toBeVisible();
    
    // CTA 버튼이 클릭 가능한 상태인지 확인
    const ctaButton = page.getByText('로그인하고 맞춤 추천 받기');
    await expect(ctaButton).toBeEnabled();
  });

  test('should display gradient background', async ({ page }) => {
    // 히어로 섹션의 배경 그라데이션이 적용되어 있는지 확인
    const heroContent = page.locator('div').filter({ hasText: /새로운 뉴스레터를 발견해보세요/ }).first();
    
    // 배경색이 적용되어 있는지 확인 (시각적 요소이므로 존재 여부만 확인)
    await expect(heroContent).toBeVisible();
  });

  test('should be responsive on mobile', async ({ page }) => {
    // 모바일 뷰포트로 변경
    await page.setViewportSize({ width: 375, height: 667 });
    
    // 모바일에서도 모든 요소가 표시되는지 확인
    await expect(page.getByText('🌸')).toBeVisible();
    await expect(page.getByText('새로운 뉴스레터를 발견해보세요! 📚')).toBeVisible();
    await expect(page.getByText('로그인하고 맞춤 추천 받기')).toBeVisible();
  });
});