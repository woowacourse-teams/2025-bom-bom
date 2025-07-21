import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import PageLayout from '../components/PageLayout/PageLayout';
import { getArticles } from '../pages/today/apis/articles';
import ArticleCardList from '../pages/today/components/ArticleCardList';
import ReadingStatusCard from '../pages/today/components/ReadingStatusCard';
import { getReadingStatus } from '../pages/today/apis/members';

export const Route = createFileRoute('/')({
  component: Index,
});

function Index() {
  const { data: articles } = useQuery({
    queryKey: ['todayArticles'],
    queryFn: () =>
      getArticles({ date: new Date(), memberId: 1, sorted: 'ASC' }),
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
            3개의 새로운 뉴스레터가 도착했어요
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
  flex-direction: column;
  align-items: flex-start;

  width: 1280px;
  padding-top: 64px;

  gap: 24px;
`;

const TitleBox = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  gap: 8px;
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
  align-items: flex-start;
  align-self: stretch;
  justify-content: center;

  gap: 24px;
`;
