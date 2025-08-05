import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import ArticleCardList from '../../pages/today/components/ArticleCardList/ArticleCardList';
import ReadingStatusCard from '../../pages/today/components/ReadingStatusCard/ReadingStatusCard';
import { getArticles } from '@/apis/articles';
import PetCard from '@/components/PetCard/PetCard';

export const Route = createFileRoute('/_bombom/')({
  component: Index,
});

function Index() {
  const today = new Date();
  const { data: articles } = useQuery({
    queryKey: ['articles', { date: today }],
    queryFn: () => getArticles({ date: today }),
  });

  return (
    <Container>
      <TitleBox>
        <Title>오늘의 뉴스레터</Title>
        <TitleDescription>
          {articles?.content?.length ?? 0}개의 새로운 뉴스레터가 도착했어요
        </TitleDescription>
      </TitleBox>
      <ContentWrapper>
        <ArticleCardList articles={articles?.content ?? []} />
        <SideCardWrapper>
          <PetCard />
          <ReadingStatusCard />
        </SideCardWrapper>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div`
  width: 1280px;
  padding-top: 64px;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;
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
  width: 100%;

  display: flex;
  gap: 24px;
  align-items: flex-start;
  align-self: stretch;
  justify-content: center;
`;

const SideCardWrapper = styled.div`
  width: 310px;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
`;
