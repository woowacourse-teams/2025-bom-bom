import { test, expect } from '@playwright/test';

test.describe('Storage 페이지 - 기본 UI 테스트', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/storage');
    await page.waitForLoadState('networkidle');

    // 리다이렉트 처리
    const currentUrl = page.url();
    if (currentUrl.includes('/recommend') || currentUrl.includes('/login')) {
      await page.goto('/storage');
      await page.waitForLoadState('networkidle');
    }
  });

  test('페이지 제목과 기본 헤더가 표시되어야 한다', async ({ page }) => {
    // 페이지 제목 확인
    await expect(
      page.getByRole('heading', { name: '뉴스레터 보관함' }),
    ).toBeVisible();

    // 제목 아이콘 확인
    await expect(page.locator('svg').first()).toBeVisible();
  });

  test('뉴스레터 필터 탭들이 표시되어야 한다', async ({ page }) => {
    // 모든 필터 탭 확인
    await expect(page.getByText('전체')).toBeVisible();
    await expect(page.getByText('테크뉴스')).toBeVisible();
    await expect(page.getByText('개발자뉴스')).toBeVisible();
    await expect(page.getByText('AI뉴스')).toBeVisible();

    // 카운트가 표시되는지 확인
    await expect(page.getByText('3').first()).toBeVisible();
    await expect(page.getByText('5').first()).toBeVisible();
  });

  test('검색 및 정렬 컨트롤이 표시되어야 한다', async ({ page }) => {
    // 검색창 확인
    await expect(page.getByRole('searchbox', { name: '검색' })).toBeVisible();

    // 정렬 옵션 확인
    await expect(page.getByRole('paragraph', { name: '최신순' })).toBeVisible();

    // 총 개수 표시 확인
    await expect(page.getByText(/총 \d+개/)).toBeVisible();
  });

  test('기사 목록이 표시되어야 한다', async ({ page }) => {
    // 기사 목록 컨테이너 확인
    await expect(page.getByRole('list')).toBeVisible();

    // 기사 항목들이 있는지 확인
    const articleItems = page.getByRole('listitem');
    await expect(articleItems.first()).toBeVisible();

    // 기사 제목이 표시되는지 확인
    await expect(page.getByRole('heading', { level: 2 }).first()).toBeVisible();
  });

  test('기사 메타 정보가 표시되어야 한다', async ({ page }) => {
    // 기사 출처 정보
    await expect(page.getByText('from UPPITY')).toBeVisible();

    // 날짜 정보
    await expect(page.getByText('2025.07.01')).toBeVisible();

    // 읽기 시간 정보
    await expect(page.getByText('5분')).toBeVisible();

    // 썸네일 이미지
    await expect(page.getByAltText('아티클 썸네일').first()).toBeVisible();
  });

  test('읽음 상태가 표시되어야 한다', async ({ page }) => {
    // 읽음 표시가 있는 기사 확인
    await expect(page.getByText('읽음')).toBeVisible();
  });
});
