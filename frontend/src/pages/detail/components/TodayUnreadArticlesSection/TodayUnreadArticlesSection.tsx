import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import EmptyUnreadCard from '../EmptyUnreadCard/EmptyUnreadCard';
import NewsletterItemCard from '../NewsletterItemCard/NewsletterItemCard';
import { queries } from '@/apis/queries';

interface TodayUnreadArticlesSectionProps {
  articleId: number;
}

const TodayUnreadArticlesSection = ({
  articleId,
}: TodayUnreadArticlesSectionProps) => {
  const today = useMemo(() => new Date(), []);
  const { data: todayArticles } = useQuery(queries.articles({ date: today }));

  const unReadArticles = todayArticles?.content?.filter(
    (article) => !article.isRead && article.articleId !== articleId,
  );

  return (
    <Container>
      <TodayArticleTitle>오늘 읽지 않은 다른 아티클</TodayArticleTitle>
      {unReadArticles?.length && unReadArticles.length > 0 ? (
        <TodayArticleList>
          {unReadArticles?.map((article) => (
            <NewsletterItemCard key={article.articleId} data={article} />
          ))}
        </TodayArticleList>
      ) : (
        <EmptyUnreadCard />
      )}
    </Container>
  );
};

export default TodayUnreadArticlesSection;

const Container = styled.div`
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
