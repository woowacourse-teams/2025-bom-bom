import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import PageLayout from '../components/PageLayout/PageLayout';
import ArticleCardList from '../pages/today/components/ArticleCardList/ArticleCardList';
import ReadingStatusCard from '../pages/today/components/ReadingStatusCard/ReadingStatusCard';
import { getArticles } from '@/apis/articles';
import { getReadingStatus } from '@/apis/members';

export const Route = createFileRoute('/')({
  component: Index,
});

function Index() {
  const { data: articles } = useQuery({
    queryKey: ['todayArticles'],
    queryFn: () => getArticles({ memberId: 1, sorted: 'DESC' }),
  });

  const { data: readingStatus } = useQuery({
    queryKey: ['readingStatus'],
    queryFn: () => getReadingStatus(1),
  });

  if (!articles || !readingStatus) return null;

  return (
    <PageLayout activeNav="today">
      <Container>
        <TitleBox>
          <Title>오늘의 뉴스레터</Title>
          <TitleDescription>
            {articles.content.length}개의 새로운 뉴스레터가 도착했어요
          </TitleDescription>
        </TitleBox>
        <ContentWrapper>
          <ArticleCardList articles={articles.content} />
          <ReadingStatusCard
            streakReadDay={readingStatus.streakReadDay}
            today={readingStatus.today}
            weekly={readingStatus.weekly}
          />
        </ContentWrapper>
      </Container>
    </PageLayout>
  );
}

const Container = styled.div`
  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;

  width: 1280px;
  padding-top: 64px;
`;

const TitleBox = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: flex-start;
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading2};
`;

const TitleDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ContentWrapper = styled.div`
  display: flex;
  gap: 24px;
  align-items: flex-start;
  align-self: stretch;
  justify-content: center;
`;
