import { test, expect } from '@playwright/test';

test.describe('Recommend Page - Reading King Leaderboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/recommend');
  });

  test('should display leaderboard header correctly', async ({ page }) => {
    // 리더보드 헤더가 표시되는지 확인
    await expect(page.getByText('이달의 독서왕')).toBeVisible();
    
    // 헤더 아이콘이 표시되는지 확인
    const headerIcon = page.locator('img, svg').first();
    if (await headerIcon.count() > 0) {
      await expect(headerIcon).toBeVisible();
    }
  });

  test('should display top 5 leaderboard correctly', async ({ page }) => {
    // 1위 사용자 확인 (크라운 아이콘과 챔피언 배지)
    await expect(page.getByText('👑')).toBeVisible();
    await expect(page.getByText('김독서')).toBeVisible();
    await expect(page.getByText('👑 챔피언')).toBeVisible();
    await expect(page.getByText('248개 읽음')).toBeVisible();
    await expect(page.getByText('+15')).toBeVisible();

    // 2위 사용자 확인
    await expect(page.getByText('🥈')).toBeVisible();
    await expect(page.getByText('박뉴스')).toBeVisible();
    await expect(page.getByText('223개 읽음')).toBeVisible();
    await expect(page.getByText('+12')).toBeVisible();

    // 3위 사용자 확인
    await expect(page.getByText('🥉')).toBeVisible();
    await expect(page.getByText('이정보')).toBeVisible();
    await expect(page.getByText('201개 읽음')).toBeVisible();
    await expect(page.getByText('+8')).toBeVisible();

    // 4위, 5위 사용자 확인
    await expect(page.getByText('#4')).toBeVisible();
    await expect(page.getByText('최트렌드')).toBeVisible();
    await expect(page.getByText('#5')).toBeVisible();
    await expect(page.getByText('정인사이트')).toBeVisible();
  });

  test('should display user rank and stats correctly', async ({ page }) => {
    // 나의 순위 섹션이 표시되는지 확인
    await expect(page.getByText('나의 순위')).toBeVisible();
    await expect(page.getByText('12위')).toBeVisible();
    
    // 읽은 뉴스레터 수가 표시되는지 확인
    await expect(page.getByText('읽은 뉴스레터')).toBeVisible();
    await expect(page.getByText('87개')).toBeVisible();
  });

  test('should display progress information correctly', async ({ page }) => {
    // 다음 순위까지의 진행 상황이 표시되는지 확인
    await expect(page.getByText('다음 순위까지')).toBeVisible();
    await expect(page.getByText('13개 더 읽기')).toBeVisible();
    
    // 진행률 바가 표시되는지 확인 (시각적 요소이므로 존재 여부만 확인)
    const progressSection = page.locator('div').filter({ hasText: /다음 순위까지/ });
    await expect(progressSection).toBeVisible();
  });

  test('should display all user avatars', async ({ page }) => {
    // 모든 사용자의 아바타 이미지가 표시되는지 확인
    const avatars = page.locator('img').filter({ hasText: '' });
    
    // 최소 5개의 아바타가 표시되는지 확인 (리더보드의 상위 5명)
    const avatarCount = await avatars.count();
    expect(avatarCount).toBeGreaterThanOrEqual(5);
  });

  test('should show proper ranking icons', async ({ page }) => {
    // 1위 크라운 아이콘
    const crownIcon = page.getByText('👑').first();
    await expect(crownIcon).toBeVisible();
    
    // 2위 은메달 아이콘
    const silverIcon = page.getByText('🥈');
    await expect(silverIcon).toBeVisible();
    
    // 3위 동메달 아이콘
    const bronzeIcon = page.getByText('🥉');
    await expect(bronzeIcon).toBeVisible();
    
    // 4위, 5위 숫자 표시
    await expect(page.getByText('#4')).toBeVisible();
    await expect(page.getByText('#5')).toBeVisible();
  });

  test('should display increment numbers correctly', async ({ page }) => {
    // 각 사용자의 증가량이 올바르게 표시되는지 확인
    const increments = ['+15', '+12', '+8', '+6', '+4'];
    
    for (const increment of increments) {
      await expect(page.getByText(increment)).toBeVisible();
    }
  });

  test('should be responsive on mobile', async ({ page }) => {
    // 모바일 뷰포트로 변경
    await page.setViewportSize({ width: 375, height: 667 });
    
    // 모바일에서도 리더보드가 표시되는지 확인
    await expect(page.getByText('이달의 독서왕')).toBeVisible();
    await expect(page.getByText('김독서')).toBeVisible();
    await expect(page.getByText('나의 순위')).toBeVisible();
    await expect(page.getByText('12위')).toBeVisible();
  });

  test('should have proper styling and layout', async ({ page }) => {
    // 리더보드 컨테이너가 올바르게 스타일링되어 있는지 확인
    const leaderboardContainer = page.locator('div').filter({ hasText: /이달의 독서왕/ }).first();
    await expect(leaderboardContainer).toBeVisible();
    
    // 사용자 순위 섹션이 올바르게 분리되어 있는지 확인
    const myRankSection = page.locator('div').filter({ hasText: /나의 순위/ });
    await expect(myRankSection).toBeVisible();
  });

  test('should display champion badge correctly', async ({ page }) => {
    // 1위 사용자의 챔피언 배지가 올바르게 표시되는지 확인
    const championBadge = page.getByText('👑 챔피언');
    await expect(championBadge).toBeVisible();
    
    // 챔피언 배지가 1위 사용자와 연결되어 있는지 확인
    const firstRankUser = page.locator('div').filter({ hasText: /김독서/ });
    await expect(firstRankUser).toBeVisible();
    await expect(firstRankUser.locator('..').getByText('👑 챔피언')).toBeVisible();
  });
});