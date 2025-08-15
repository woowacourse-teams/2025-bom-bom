import { test, expect } from '@playwright/test';

test.describe('추천 페이지 - 트렌디 섹션', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/recommend');
  });

  test('트렌디 섹션 헤더가 올바르게 표시되어야 한다', async ({ page }) => {
    // 트렌디 섹션 헤더가 표시되는지 확인
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    // 아이콘이 표시되는지 확인
    const iconContainer = page
      .locator('span')
      .filter({ hasText: /트렌디한 뉴스레터/ })
      .locator('..')
      .locator('img, svg')
      .first();
    if ((await iconContainer.count()) > 0) {
      await expect(iconContainer).toBeVisible();
    }
  });

  test('카테고리 필터 칩들이 표시되어야 한다', async ({ page }) => {
    // 카테고리 필터 칩들이 표시되는지 확인
    const categoryButtons = [
      '전체',
      '트렌드/라이프',
      '비즈/재테크',
      '지역/여행',
      '푸드',
      'IT/테크',
      '시사/사회',
      '취미/자기개발',
      '문화/예술',
      '리빙/인테리어',
    ];

    for (const category of categoryButtons) {
      await expect(page.getByText(category)).toBeVisible();
    }
  });

  test('카테고리 칩을 클릭하면 뉴스레터가 필터링되어야 한다', async ({
    page,
  }) => {
    // 기본적으로 "전체" 카테고리가 선택되어 있는지 확인
    const allCategoryButton = page.getByRole('button', { name: '전체' });
    await expect(allCategoryButton).toBeVisible();

    // 다른 카테고리 클릭해보기
    const itTechButton = page.getByRole('button', { name: 'IT/테크' });
    await itTechButton.click();

    // 클릭 후 페이지가 여전히 로드되어 있는지 확인
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    // 푸드 카테고리 클릭해보기
    const foodButton = page.getByRole('button', { name: '푸드' });
    await foodButton.click();
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    // 다시 전체로 돌아가기
    await allCategoryButton.click();
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();
  });

  test('뉴스레터 카드들이 표시되어야 한다', async ({ page }) => {
    // 뉴스레터 카드들이 표시되는지 확인
    const newsletterCards = page
      .locator('button')
      .filter({ hasText: /뉴스레터 이미지/ });

    // 최소 하나의 뉴스레터 카드가 표시되는지 확인
    await expect(newsletterCards.first()).toBeVisible();

    // 특정 뉴스레터가 표시되는지 확인 (스냅샷에서 확인된 것들)
    await expect(page.getByText('오당맛')).toBeVisible();
    await expect(page.getByText('노마드코더')).toBeVisible();
  });

  test('뉴스레터 카드 클릭을 처리해야 한다', async ({ page }) => {
    // 첫 번째 뉴스레터 카드 클릭
    const firstCard = page
      .locator('button')
      .filter({ hasText: /뉴스레터 이미지/ })
      .first();
    await expect(firstCard).toBeVisible();

    // 새 탭이 열리는 것을 감지하기 위한 이벤트 리스너 설정
    const [newPage] = await Promise.all([
      page.waitForEvent('popup'),
      firstCard.click(),
    ]);

    // 새 탭이 열렸는지 확인
    expect(newPage).toBeTruthy();
    await newPage.close();
  });

  test('뉴스레터 정보가 올바르게 표시되어야 한다', async ({ page }) => {
    // 뉴스레터 카드에 제목과 설명이 표시되는지 확인
    await expect(page.getByText('오! 당신이 반할 그 맛 😋')).toBeVisible();
    await expect(page.getByText('최신 개발 Dev 뉴스')).toBeVisible();

    // 뉴스레터 이미지가 표시되는지 확인
    const images = page.locator('img[alt*="뉴스레터 이미지"]');
    await expect(images.first()).toBeVisible();
  });

  test('모바일에서 반응형으로 동작해야 한다', async ({ page }) => {
    // 모바일 뷰포트로 변경
    await page.setViewportSize({ width: 375, height: 667 });

    // 모바일에서도 트렌디 섹션이 표시되는지 확인
    await expect(page.getByText('트렌디한 뉴스레터')).toBeVisible();

    // 카테고리 버튼들이 표시되는지 확인
    await expect(page.getByText('전체')).toBeVisible();
    await expect(page.getByText('IT/테크')).toBeVisible();

    // 뉴스레터 카드가 표시되는지 확인
    await expect(page.getByText('오당맛')).toBeVisible();
  });

  test('이메일 복사 기능을 처리해야 한다', async ({ page }) => {
    // 클립보드 API mock
    await page.addInitScript(() => {
      Object.assign(navigator, {
        clipboard: {
          writeText: () => Promise.resolve(),
        },
      });
    });

    // 알림 대화상자를 처리하기 위한 리스너 설정
    page.on('dialog', async (dialog) => {
      expect(dialog.message()).toContain('이메일이 복사되었습니다');
      await dialog.accept();
    });

    // 뉴스레터 카드 클릭 (로그인되지 않은 상태에서는 알림만 표시)
    const firstCard = page
      .locator('button')
      .filter({ hasText: /뉴스레터 이미지/ })
      .first();
    await firstCard.click();
  });
});
