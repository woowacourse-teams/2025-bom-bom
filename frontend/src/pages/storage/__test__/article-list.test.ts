import { test, expect } from '@playwright/test';

test.describe('보관함 페이지 - 아티클 목록 및 읽음 상태', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3000/storage');
    await page.waitForLoadState('networkidle');
  });

  test('아티클 목록이 올바르게 표시된다', async ({ page }) => {
    // 아티클 아이템들이 표시되는지 확인 (from 텍스트로 아티클만 필터링)
    const articleItems = page.getByRole('listitem').filter({ hasText: 'from' });
    await expect(articleItems).toHaveCount(6); // 첫 페이지에 6개 아티클

    // 첫 번째 아티클이 표시되는지 확인
    await expect(articleItems.first()).toBeVisible();
  });

  test('아티클 기본 정보가 표시된다', async ({ page }) => {
    // 아티클만 필터링해서 첫 번째 아티클 선택
    const firstArticle = page
      .getByRole('listitem')
      .filter({ hasText: 'from' })
      .first();

    // 제목 확인 (heading 대신 h2 태그 확인)
    await expect(firstArticle.locator('h2')).toBeVisible();

    // 뉴스레터 출처 확인 (from XX)
    await expect(firstArticle.getByText(/^from /)).toBeVisible();

    // 날짜 확인 (YYYY.MM.DD 형식)
    await expect(firstArticle.getByText(/^\d{4}\.\d{2}\.\d{2}$/)).toBeVisible();

    // 읽는 시간 확인 (X분)
    await expect(firstArticle.getByText(/^\d+분$/)).toBeVisible();

    // 썸네일 이미지 확인
    await expect(firstArticle.getByAltText('아티클 썸네일')).toBeVisible();
  });

  test('읽음 상태가 올바르게 표시된다', async ({ page }) => {
    // 읽은 아티클의 "읽음" 표시 확인
    const readArticles = page.getByText('읽음');
    const readCount = await readArticles.count();
    expect(readCount).toBeGreaterThan(0); // 적어도 1개의 읽은 아티클이 있어야 함

    // 특정 읽은 아티클 확인
    if (readCount > 0) {
      const readArticle = page
        .getByRole('listitem')
        .filter({ hasText: '읽음' })
        .first();
      await expect(readArticle.getByText('읽음')).toBeVisible();
    }
  });

  test('읽지 않은 아티클에는 읽음 표시가 없다', async ({ page }) => {
    // 읽지 않은 아티클 찾기
    const unreadArticles = page
      .getByRole('listitem')
      .filter({ hasText: '테크뉴스: 이번 주 IT 핫이슈' });

    // 읽음 표시가 없는지 확인
    const unreadCount = await unreadArticles.count();
    if (unreadCount > 0) {
      await expect(unreadArticles.first().getByText('읽음')).not.toBeVisible();
    }
  });

  test('아티클 클릭 시 상세 페이지로 이동한다', async ({ page }) => {
    // 첫 번째 아티클의 링크 확인 (아티클만 필터링)
    const firstArticle = page
      .getByRole('listitem')
      .filter({ hasText: 'from' })
      .first();
    const firstArticleLink = firstArticle.locator('a').first();

    // 링크가 존재하는지 확인
    await expect(firstArticleLink).toBeVisible();

    // 링크가 올바른 URL을 가지는지 확인
    const href = await firstArticleLink.getAttribute('href');
    expect(href).toMatch(/^\/articles\/\d+$/);
  });

  test('아티클 정보 일관성 확인', async ({ page }) => {
    // 아티클만 필터링
    const articleItems = page.getByRole('listitem').filter({ hasText: 'from' });
    const count = await articleItems.count();

    // 각 아티클이 필수 정보를 모두 가지고 있는지 확인
    for (let i = 0; i < Math.min(count, 3); i++) {
      // 처음 3개만 확인
      const article = articleItems.nth(i);

      // 제목이 있는지 확인 (h2 태그)
      await expect(article.locator('h2')).toBeVisible();

      // 뉴스레터 출처가 있는지 확인
      await expect(article.getByText(/^from /)).toBeVisible();

      // 날짜가 있는지 확인
      await expect(article.getByText(/^\d{4}\.\d{2}\.\d{2}$/)).toBeVisible();

      // 읽는 시간이 있는지 확인
      await expect(article.getByText(/^\d+분$/)).toBeVisible();
    }
  });

  test('아티클 썸네일이 로드된다', async ({ page }) => {
    const thumbnails = page.getByAltText('아티클 썸네일');

    // 썸네일이 여러 개 있는지 확인
    const thumbnailCount = await thumbnails.count();
    expect(thumbnailCount).toBeGreaterThan(0);

    // 첫 번째 썸네일이 로드되었는지 확인
    if (thumbnailCount > 0) {
      const firstThumbnail = thumbnails.first();
      await expect(firstThumbnail).toBeVisible();
    }
  });

  test('총 아티클 개수가 올바르게 표시된다', async ({ page }) => {
    // 기본 상태에서 총 개수 확인
    await expect(page.getByText('총 7개')).toBeVisible();

    // 개수 텍스트가 정확한 형식인지 확인
    const countText = page.getByText(/^총 \d+개$/);
    await expect(countText).toBeVisible();
  });
});
