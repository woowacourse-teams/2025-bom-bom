import styled from '@emotion/styled';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useState } from 'react';
import { getArticleById, getArticles, patchArticleRead } from '@/apis/articles';
import Chip from '@/components/Chip/Chip';
import Spacing from '@/components/Spacing/Spacing';
import { useScrollThreshold } from '@/hooks/useScrollThreshold';
import EmptyUnreadCard from '@/pages/detail/components/EmptyUnreadCard/EmptyUnreadCard';
import MemoPanel from '@/pages/detail/components/MemoPanel/MemoPanel';
import FloatingToolbar from '@/pages/detail/components/FloatingToolbar/FloatingToolbar';
import NewsletterItemCard from '@/pages/detail/components/NewsletterItemCard/NewsletterItemCard';
import { useHighlightManager } from '@/pages/detail/hooks/useHighlightManager';
import { saveSelection } from '@/pages/detail/utils/highlight';
import { formatDate } from '@/utils/date';
import ClockIcon from '#/assets/clock.svg';

export const Route = createFileRoute('/_bombom/articles/$articleId')({
  component: ArticleDetailPage,
});

function ArticleDetailPage() {
  const { articleId } = Route.useParams();
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(true);
  const [memo, setMemo] = useState('');
  const { highlights, addHighlights } = useHighlightManager();

  const { data: currentArticle } = useQuery({
    queryKey: ['article', articleId],
    queryFn: () =>
      getArticleById({
        articleId: Number(articleId),
      }),
  });
  const { data: otherArticles } = useQuery({
    queryKey: ['otherArticles'],
    queryFn: () => getArticles({ date: new Date(), sorted: 'ASC' }),
  });
  const { mutate: updateArticleAsRead } = useMutation({
    mutationKey: ['read', articleId],
    mutationFn: () => patchArticleRead({ articleId: Number(articleId) }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['article', articleId],
      });
    },
  });

  useScrollThreshold({
    enabled: !currentArticle?.isRead && !!currentArticle,
    threshold: 70,
    throttleMs: 500,
    onTrigger: updateArticleAsRead,
  });

  if (!currentArticle || !otherArticles) return null;

  const unReadArticles = otherArticles?.content.filter(
    (article) => !article.isRead && article.articleId !== Number(articleId),
  );

  return (
    <Container>
      <HeaderWrapper>
        <button onClick={() => setOpen((open) => !open)}>열기</button>
        <Title>{currentArticle.title}</Title>
        <MetaInfoRow>
          <Chip text={currentArticle.newsletter.category} />
          <MetaInfoText>from {currentArticle.newsletter.name}</MetaInfoText>
          <MetaInfoText>
            {formatDate(new Date(currentArticle.arrivedDateTime))}
          </MetaInfoText>
          <ReadTimeBox>
            <ClockIcon width={16} height={16} />
            <MetaInfoText>{currentArticle.expectedReadTime}분</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </HeaderWrapper>
      <Divider />
      <ContentWrapper
        dangerouslySetInnerHTML={{ __html: currentArticle.contents ?? '' }}
      />
      <Spacing size={24} />
      <Divider />
      <ContentDescription>
        이 뉴스레터가 유용했다면 동료들과 공유해주세요. 피드백이나 제안사항이
        있으시면 언제든 연락 주시기 바랍니다.
      </ContentDescription>
      <TodayArticlesWrapper>
        <TodayArticleTitle>오늘 읽지 않은 다른 아티클</TodayArticleTitle>
        {unReadArticles.length > 0 ? (
          <TodayArticleList>
            {unReadArticles?.map((article) => (
              <NewsletterItemCard key={article.articleId} data={article} />
            ))}
          </TodayArticleList>
        ) : (
          <EmptyUnreadCard />
        )}
      </TodayArticlesWrapper>
      <FloatingToolbar
        onSave={(selection) => {
          const highlightData = saveSelection(selection);
          addHighlights(highlightData);
        }}
      />
      <MemoPanel
        open={open}
        handleClose={() => setOpen(false)}
        notes={[{ id: '1', content: 'content', memo }]}
        handleDeleteMemo={(id) => console.log(id)}
        handleUpdateMemo={(id, e) => console.log(id, setMemo(e.target.value))}
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

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const ContentDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const TodayArticlesWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  flex-direction: column;
`;

const TodayArticleTitle = styled.h3`
  align-self: flex-start;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading4};
`;

const TodayArticleList = styled.div`
  display: grid;
  gap: 20px;

  grid-template-columns: repeat(2, 1fr);
`;
