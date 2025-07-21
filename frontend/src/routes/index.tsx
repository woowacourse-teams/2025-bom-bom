import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import PageLayout from '../components/PageLayout/PageLayout';
import ArticleCardList from '../pages/today/components/ArticleCardList/ArticleCardList';
import ReadingStatusCard from '../pages/today/components/ReadingStatusCard/ReadingStatusCard';
import { ARTICLES } from '../mocks/data/mock-articles';

export const Route = createFileRoute('/')({
  component: Index,
});

function Index() {
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
          <ArticleCardList articles={ARTICLES} />
          <ReadingStatusCard
            streakReadDay={267}
            today={{ readCount: 3, totalCount: 5 }}
            weekly={{ readCount: 12, goalCount: 15 }}
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
