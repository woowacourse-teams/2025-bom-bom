import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useRef } from 'react';
import { queries } from '@/apis/queries';
import DetailPageHeader from '@/components/Header/DetailPageHeader';
import ProgressBar from '@/components/ProgressBar/ProgressBar';
import Spacing from '@/components/Spacing/Spacing';
import { useDevice } from '@/hooks/useDevice';
import useScrollProgress from '@/hooks/useScrollProgress';
import useScrollRestoration from '@/hooks/useScrollRestoration';
import { useScrollThreshold } from '@/hooks/useScrollThreshold';
import ArticleBody from '@/pages/detail/components/ArticleBody/ArticleBody';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import FloatingActionButtons from '@/pages/detail/components/FloatingActionButtons/FloatingActionButtons';
import TodayUnreadArticlesSection from '@/pages/detail/components/TodayUnreadArticlesSection/TodayUnreadArticlesSection';
import useArticleAsReadMutation from '@/pages/detail/hooks/useArticleAsReadMutation';
import { useArticleBookmark } from '@/pages/detail/hooks/useArticleBookmark';
import { openExternalLink } from '@/utils/externalLink';
import type { Device } from '@/hooks/useDevice';

export const Route = createFileRoute('/_bombom/articles/$articleId')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
      {
        title: '봄봄 | 아티클 상세',
      },
    ],
  }),
  component: ArticleDetailPage,
});

function ArticleDetailPage() {
  const { articleId } = Route.useParams();
  const articleIdNumber = Number(articleId);
  const device = useDevice();
  const articleContentRef = useRef<HTMLDivElement>(null);

  const { data: currentArticle } = useQuery(
    queries.articleById({ id: articleIdNumber }),
  );
  const { mutate: updateArticleAsRead } = useArticleAsReadMutation({
    articleId: articleIdNumber,
  });
  const { isBookmarked, toggleBookmark } = useArticleBookmark({
    articleId: articleIdNumber,
  });

  const { progressPercentage } = useScrollProgress();

  useScrollThreshold({
    enabled: !currentArticle?.isRead && !!currentArticle,
    threshold: 70,
    throttleMs: 500,
    onTrigger: updateArticleAsRead,
  });

  useScrollRestoration({ pathname: articleId, enabled: !!currentArticle });

  useEffect(() => {
    const contentEl = articleContentRef.current;
    if (!contentEl) return;

    const handleClick = (e: MouseEvent) => {
      const link = (e.target as HTMLElement).closest('a');
      if (link && link.href) {
        e.preventDefault();
        openExternalLink(link.href);
      }
    };

    contentEl.addEventListener('click', handleClick);
    return () => contentEl.removeEventListener('click', handleClick);
  }, [currentArticle]);

  if (!currentArticle) return null;

  return (
    <>
      {device !== 'pc' && (
        <DetailPageHeader
          bookmarked={isBookmarked}
          onBookmarkClick={toggleBookmark}
        />
      )}

      <Container>
        <ArticleContent ref={articleContentRef} device={device}>
          <ArticleProgressBar
            rate={progressPercentage}
            transition={false}
            variant="rectangular"
            device={device}
          />
          <ArticleHeader
            title={currentArticle.title ?? ''}
            newsletterCategory={currentArticle.newsletter?.category ?? ''}
            newsletterName={currentArticle.newsletter?.name ?? ''}
            arrivedDateTime={new Date(currentArticle.arrivedDateTime ?? '')}
            expectedReadTime={currentArticle.expectedReadTime ?? 1}
          />
          <Divider />

          <ArticleBody
            articleId={articleIdNumber}
            newsletterName={currentArticle.newsletter.name}
            articleContent={currentArticle.contents}
          />
          <Spacing size={24} />
          <Divider />

          <ContentDescription>
            이 뉴스레터가 유용했다면 동료들과 공유해주세요. 피드백이나
            제안사항이 있으시면 언제든 연락 주시기 바랍니다.
          </ContentDescription>

          <TodayUnreadArticlesSection articleId={articleIdNumber} />
        </ArticleContent>

        {device === 'pc' && (
          <ArticleActionButtons
            bookmarked={isBookmarked}
            onBookmarkClick={toggleBookmark}
          />
        )}
      </Container>
    </>
  );
}

const Container = styled.div`
  position: relative;
`;

const ArticleActionButtons = styled(FloatingActionButtons)`
  position: fixed;
  top: 80vh;

  transform: translate(-200%, -50%);
`;

const ArticleContent = styled.div<{ device: Device }>`
  max-width: 700px;
  margin: 0 auto;
  padding: ${({ device }) => (device === 'mobile' ? '0' : '0 16px')};
  border-right: 1px solid
    ${({ theme, device }) =>
      device === 'mobile' ? 'transparent' : theme.colors.stroke};
  border-left: 1px solid
    ${({ theme, device }) =>
      device === 'mobile' ? 'transparent' : theme.colors.stroke};

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
`;

const ArticleProgressBar = styled(ProgressBar)<{ device: Device }>`
  position: fixed;
  top: ${({ device, theme }) =>
    device === 'pc'
      ? `calc(${theme.heights.headerPC} + env(safe-area-inset-top))`
      : `calc(${theme.heights.headerMobile} + env(safe-area-inset-top))`};
  z-index: ${({ theme }) => theme.zIndex.floating};
  height: 4px;
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const ContentDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;
