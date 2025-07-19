import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import PageLayout from '../components/PageLayout/PageLayout';
import ArticleCardList from '../pages/today/components/ArticleCardList';
import ReadingStatusCard from '../pages/today/components/ReadingStatusCard';

export const Route = createFileRoute('/')({
  component: Index,
});

// mock data
const ARTICLES = [
  {
    articleId: '1',
    title: '폭염에도 전력난 없는 이유는?',
    contentsSummary:
      '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
    arrivedDateTime: new Date('2025.07.01'),
    thumbnailUrl:
      'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
    expectedReadTime: 5,
    isRead: false,
    newsletter: {
      category: '기술',
      name: 'UPPITY',
      imageUrl: 'https://example.com/newsletter-image.jpg',
    },
  },
  {
    articleId: '2',
    title: '폭염에도 전력난 없는 이유는?',
    contentsSummary:
      '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
    arrivedDateTime: new Date('2025.07.01'),
    thumbnailUrl:
      'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
    expectedReadTime: 5,
    isRead: true,
    newsletter: {
      category: '기술',
      name: 'UPPITY',
      imageUrl: 'https://example.com/newsletter-image.jpg',
    },
  },
  {
    articleId: '3',
    title: '폭염에도 전력난 없는 이유는?',
    contentsSummary:
      '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
    arrivedDateTime: new Date('2025.07.01'),
    thumbnailUrl:
      'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
    expectedReadTime: 5,
    isRead: false,
    newsletter: {
      category: '기술',
      name: 'UPPITY',
      imageUrl: 'https://example.com/newsletter-image.jpg',
    },
  },
];

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
