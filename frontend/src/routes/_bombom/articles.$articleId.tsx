import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useState } from 'react';
import { queries } from '@/apis/queries';
import Chip from '@/components/Chip/Chip';
import Spacing from '@/components/Spacing/Spacing';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import useScrollRestoration from '@/hooks/useScrollRestoration';
import { useScrollThreshold } from '@/hooks/useScrollThreshold';
import ArticleBody from '@/pages/detail/components/ArticleBody/ArticleBody';
import FloatingActionButtons from '@/pages/detail/components/FloatingActionButtons/FloatingActionButtons';
import TodayUnreadArticlesSection from '@/pages/detail/components/TodayUnreadArticlesSection/TodayUnreadArticlesSection';
import useBookmarkMutation from '@/pages/detail/hooks/useBookmarkMutation';
import useMarkArticleAsReadMutation from '@/pages/detail/hooks/useMarkArticleAsReadMutation';
import { formatDate } from '@/utils/date';
import ClockIcon from '#/assets/clock.svg';

export const Route = createFileRoute('/_bombom/articles/$articleId')({
  component: ArticleDetailPage,
});

function ArticleDetailPage() {
  const { articleId } = Route.useParams();
  const articleIdNumber = Number(articleId);

  const { data: currentArticle } = useQuery(
    queries.articleById({ id: articleIdNumber }),
  );
  const { mutate: updateArticleAsRead } = useMarkArticleAsReadMutation({
    articleId: articleIdNumber,
  });
  const { data: bookmarked } = useQuery(
    queries.bookmarkStatusByArticleId({ articleId: articleIdNumber }),
  );
  const [isBookmarked, setIsBookmarked] = useState(false);
  useEffect(() => {
    setIsBookmarked(bookmarked?.bookmarkStatus ?? false);
  }, [bookmarked?.bookmarkStatus]);
  const { toggleBookmark } = useBookmarkMutation({
    articleId: articleIdNumber,
  });

  const onBookmarkClick = () => {
    setIsBookmarked((prev) => !prev);
    toggleBookmark(debouncedBookmark);
  };
  const debouncedBookmark = useDebouncedValue(isBookmarked, 500);

  useScrollThreshold({
    enabled: !currentArticle?.isRead && !!currentArticle,
    threshold: 70,
    throttleMs: 500,
    onTrigger: updateArticleAsRead,
  });

  useScrollRestoration({ pathname: articleId });

  if (!currentArticle) return null;

  return (
    <Container>
      <HeaderWrapper>
        <Title>{currentArticle.title}</Title>
        <MetaInfoRow>
          <Chip text={currentArticle.newsletter?.category ?? ''} />
          <MetaInfoText>
            from {currentArticle.newsletter?.name ?? ''}
          </MetaInfoText>
          <MetaInfoText>
            {formatDate(new Date(currentArticle.arrivedDateTime ?? ''))}
          </MetaInfoText>
          <ReadTimeBox>
            <ClockIcon width={16} height={16} />
            <MetaInfoText>{currentArticle.expectedReadTime}분</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </HeaderWrapper>
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
        onBookmarkClick={onBookmarkClick}
      />
    </Container>
  );
}

const Container = styled.div`
  max-width: 700px;
  margin: 0 auto;
  margin-top: 20px;
  padding: 28px;
  border-right: 1px solid ${({ theme }) => theme.colors.stroke};
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
`;

const HeaderWrapper = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading2};
`;

const MetaInfoRow = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const MetaInfoText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;
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
