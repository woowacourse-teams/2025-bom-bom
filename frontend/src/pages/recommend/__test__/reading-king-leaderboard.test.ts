import { test, expect } from '@playwright/test';

test.describe('추천 페이지 - 독서왕 리더보드', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/recommend');
  });

  test('리더보드 헤더가 올바르게 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('이달의 독서왕')).toBeVisible();

    const headerIcon = page.locator('img, svg').first();
    if ((await headerIcon.count()) > 0) {
      await expect(headerIcon).toBeVisible();
    }
  });

  test('상위 5명 리더보드가 올바르게 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('👑')).toBeVisible();
    await expect(page.getByText('김독서')).toBeVisible();
    await expect(page.getByText('👑 챔피언')).toBeVisible();
    await expect(page.getByText('248개 읽음')).toBeVisible();
    await expect(page.getByText('+15')).toBeVisible();

    await expect(page.getByText('🥈')).toBeVisible();
    await expect(page.getByText('박뉴스')).toBeVisible();
    await expect(page.getByText('223개 읽음')).toBeVisible();
    await expect(page.getByText('+12')).toBeVisible();

    await expect(page.getByText('🥉')).toBeVisible();
    await expect(page.getByText('이정보')).toBeVisible();
    await expect(page.getByText('201개 읽음')).toBeVisible();
    await expect(page.getByText('+8')).toBeVisible();

    await expect(page.getByText('#4')).toBeVisible();
    await expect(page.getByText('최트렌드')).toBeVisible();
    await expect(page.getByText('#5')).toBeVisible();
    await expect(page.getByText('정인사이트')).toBeVisible();
  });

  test('사용자 순위와 통계가 올바르게 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('나의 순위')).toBeVisible();
    await expect(page.getByText('12위')).toBeVisible();

    await expect(page.getByText('읽은 뉴스레터')).toBeVisible();
    await expect(page.getByText('87개')).toBeVisible();
  });

  test('진행 정보가 올바르게 표시되어야 한다', async ({ page }) => {
    await expect(page.getByText('다음 순위까지')).toBeVisible();
    await expect(page.getByText('13개 더 읽기')).toBeVisible();

    const progressSection = page
      .locator('div')
      .filter({ hasText: /다음 순위까지/ });
    await expect(progressSection).toBeVisible();
  });

  test('모든 사용자 아바타가 표시되어야 한다', async ({ page }) => {
    const avatars = page.locator('img').filter({ hasText: '' });

    const avatarCount = await avatars.count();
    expect(avatarCount).toBeGreaterThanOrEqual(5);
  });

  test('적절한 순위 아이콘들이 표시되어야 한다', async ({ page }) => {
    const crownIcon = page.getByText('👑').first();
    await expect(crownIcon).toBeVisible();

    const silverIcon = page.getByText('🥈');
    await expect(silverIcon).toBeVisible();

    const bronzeIcon = page.getByText('🥉');
    await expect(bronzeIcon).toBeVisible();

    await expect(page.getByText('#4')).toBeVisible();
    await expect(page.getByText('#5')).toBeVisible();
  });

  test('증가 수치가 올바르게 표시되어야 한다', async ({ page }) => {
    const increments = ['+15', '+12', '+8', '+6', '+4'];

    for (const increment of increments) {
      await expect(page.getByText(increment)).toBeVisible();
    }
  });

  test('모바일에서 반응형으로 동작해야 한다', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });

    await expect(page.getByText('이달의 독서왕')).toBeVisible();
    await expect(page.getByText('김독서')).toBeVisible();
    await expect(page.getByText('나의 순위')).toBeVisible();
    await expect(page.getByText('12위')).toBeVisible();
  });

  test('올바른 스타일과 레이아웃을 가져야 한다', async ({ page }) => {
    const leaderboardContainer = page
      .locator('div')
      .filter({ hasText: /이달의 독서왕/ })
      .first();
    await expect(leaderboardContainer).toBeVisible();

    const myRankSection = page.locator('div').filter({ hasText: /나의 순위/ });
    await expect(myRankSection).toBeVisible();
  });

  test('챔피언 배지가 올바르게 표시되어야 한다', async ({ page }) => {
    const championBadge = page.getByText('👑 챔피언');
    await expect(championBadge).toBeVisible();

    const firstRankUser = page.locator('div').filter({ hasText: /김독서/ });
    await expect(firstRankUser).toBeVisible();
    await expect(
      firstRankUser.locator('..').getByText('👑 챔피언'),
    ).toBeVisible();
  });
});
