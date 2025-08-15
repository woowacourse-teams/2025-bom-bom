import { test, expect } from '@playwright/test';

test.describe('보관함 페이지 - 검색 기능', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/storage');
    await page.waitForLoadState('networkidle');
  });

  test('검색어 입력 시 관련 아티클이 필터링된다', async ({ page }) => {
    const searchInput = page.getByRole('searchbox', { name: '검색' });

    // AI로 검색
    await searchInput.fill('AI');
    await page.waitForTimeout(2000); // 디바운스 대기

    // 검색 결과 확인
    await expect(page.getByText('총 1개')).toBeVisible();
    await expect(page.getByText('AI가 바꿀 미래의 일자리')).toBeVisible();
    await expect(page.getByText('from AI뉴스')).toBeVisible();
  });

  test('존재하지 않는 검색어 입력 시 결과가 없다', async ({ page }) => {
    const searchInput = page.getByRole('searchbox', { name: '검색' });

    // 존재하지 않는 검색어 입력
    await searchInput.fill('존재하지않는검색어');
    await page.waitForTimeout(2000);

    // 결과 없음 확인
    await expect(page.getByText('총 0개')).toBeVisible();

    // 아티클 목록이 비어있는지 확인
    const articleItems = page.locator('list listitem');
    await expect(articleItems).toHaveCount(0);
  });

  test('검색어 조합 테스트 - 개발자', async ({ page }) => {
    const searchInput = page.getByRole('searchbox', { name: '검색' });

    await searchInput.fill('개발자');
    await page.waitForTimeout(2000);

    // 개발자 관련 아티클 확인
    await expect(page.getByText('총 1개')).toBeVisible();
    await expect(
      page.getByText('개발자들을 위한 생산성 도구 TOP 5'),
    ).toBeVisible();
  });

  test('검색어 조합 테스트 - 테크뉴스', async ({ page }) => {
    const searchInput = page.getByRole('searchbox', { name: '검색' });

    await searchInput.fill('테크뉴스');
    await page.waitForTimeout(2000);

    // 테크뉴스 관련 아티클 확인
    await expect(page.getByText('총 1개')).toBeVisible();
    await expect(page.getByText('테크뉴스: 이번 주 IT 핫이슈')).toBeVisible();
  });

  test('검색어 지우기 시 전체 목록이 다시 표시된다', async ({ page }) => {
    const searchInput = page.getByRole('searchbox', { name: '검색' });

    // 검색어 입력
    await searchInput.fill('AI');
    await page.waitForTimeout(2000);
    await expect(page.getByText('총 1개')).toBeVisible();

    // 검색어 지우기
    await searchInput.fill('');
    await page.waitForTimeout(2000);

    // 전체 목록 다시 표시 확인
    await expect(page.getByText('총 7개')).toBeVisible();
  });

  test('뉴스레터 필터와 검색이 함께 작동한다', async ({ page }) => {
    // UPPITY 필터 선택
    await page.getByText('UPPITY3').click();
    await page.waitForLoadState('networkidle');
    await expect(page.getByText('총 3개')).toBeVisible();

    // AI로 검색 (UPPITY에는 AI 관련 아티클이 없으므로)
    const searchInput = page.getByRole('searchbox', { name: '검색' });
    await searchInput.fill('AI');
    await page.waitForTimeout(2000);

    // 검색 결과 없음 확인
    await expect(page.getByText('총 0개')).toBeVisible();

    // 전체 탭으로 돌아가서 같은 검색어로 검색
    await page.getByText('전체7').click();
    await page.waitForLoadState('networkidle');

    // AI 검색 결과 확인
    await expect(page.getByText('총 1개')).toBeVisible();
    await expect(page.getByText('AI가 바꿀 미래의 일자리')).toBeVisible();
  });
});
