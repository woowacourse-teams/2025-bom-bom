import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import ProgressBar from '@/components/ProgressBar/ProgressBar';
import Spacing from '@/components/Spacing/Spacing';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import useScrollProgress from '@/hooks/useScrollProgress';
import useScrollRestoration from '@/hooks/useScrollRestoration';
import { useScrollThreshold } from '@/hooks/useScrollThreshold';
import ArticleBody from '@/pages/detail/components/ArticleBody/ArticleBody';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import FloatingActionButtons from '@/pages/detail/components/FloatingActionButtons/FloatingActionButtons';
import TodayUnreadArticlesSection from '@/pages/detail/components/TodayUnreadArticlesSection/TodayUnreadArticlesSection';
import useArticleAsReadMutation from '@/pages/detail/hooks/useArticleAsReadMutation';
import { useArticleBookmark } from '@/pages/detail/hooks/useArticleBookmark';

export const Route = createFileRoute('/_bombom/articles/$articleId')({
  component: ArticleDetailPage,
});

function ArticleDetailPage() {
  const { articleId } = Route.useParams();
  const articleIdNumber = Number(articleId);
  const deviceType = useDeviceType();

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

  useScrollRestoration({ pathname: articleId });

  if (!currentArticle) return null;

  return (
    <>
      <ArticleProgressBar
        rate={progressPercentage}
        transition={false}
        variant="rectangular"
        deviceType={deviceType}
      />
      <Container deviceType={deviceType}>
        <ArticleHeader
          title={currentArticle.title ?? ''}
          newsletterCategory={currentArticle.newsletter?.category ?? ''}
          newsletterName={currentArticle.newsletter?.name ?? ''}
          arrivedDateTime={new Date(currentArticle.arrivedDateTime ?? '')}
          expectedReadTime={currentArticle.expectedReadTime ?? 1}
          bookmarked={isBookmarked}
          onBookmarkClick={toggleBookmark}
        />
        <Divider />

        <ArticleBody
          articleId={articleIdNumber}
          articleContent={currentArticle.contents}
        />
        <Spacing size={24} />
        <Divider />

        <ContentDescription>
          이 뉴스레터가 유용했다면 동료들과 공유해주세요. 피드백이나 제안사항이
          있으시면 언제든 연락 주시기 바랍니다.
        </ContentDescription>

        <TodayUnreadArticlesSection articleId={articleIdNumber} />

        <FloatingActionButtons
          bookmarked={isBookmarked}
          onBookmarkClick={toggleBookmark}
        />
      </Container>
    </>
  );
}

const Container = styled.div<{ deviceType: DeviceType }>`
  max-width: 700px;
  margin: 0 auto;
  margin-top: 20px;
  padding: ${({ deviceType }) => (deviceType === 'mobile' ? '0' : '0 16px')};
  border-right: 1px solid
    ${({ theme, deviceType }) =>
      deviceType === 'mobile' ? 'transparent' : theme.colors.stroke};
  border-left: 1px solid
    ${({ theme, deviceType }) =>
      deviceType === 'mobile' ? 'transparent' : theme.colors.stroke};

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
`;

const ArticleProgressBar = styled(ProgressBar)<{ deviceType: DeviceType }>`
  position: fixed;
  top: ${({ deviceType, theme }) =>
    deviceType === 'mobile'
      ? `calc(${theme.heights.headerMobile} + env(safe-area-inset-top))`
      : `calc(${theme.heights.headerPC} + env(safe-area-inset-top))`};
  z-index: ${({ theme }) => theme.zIndex.floating};
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
