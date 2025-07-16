import styled from '@emotion/styled';
import { ThemeProvider } from '@emotion/react';
import { theme } from './styles/theme';
import ArticleCard from './pages/today/components/ArticleCard';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <Container>Hello World</Container>
      <ArticleCard
        data={{
          articleId: '1',
          title: '폭염에도 전력난 없는 이유는?',
          contentsSummary:
            '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
          arrivedDateTime: new Date('2025.07.01'),
          thumbnailUrl:
            'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
          expectedReadTime: '5분',
          isRead: false,
          newsletter: {
            category: '기술',
            name: 'UPPITY',
            imageUrl: 'https://example.com/newsletter-image.jpg',
          },
        }}
      />
      <div style={{ height: '12px' }} />
      <ArticleCard
        data={{
          articleId: '2',
          title: '폭염에도 전력난 없는 이유는?',
          contentsSummary:
            '자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간 자동차 사이버보안 소프트웨어 솔루션 기업인 아우토크립트가 코스닥에 새로 상장해요. 15일 오늘까지 폴더블 스마트폰에 적용되는 초박형 강화유리를 주력사업으로 하는 도우인시스의 코스닥 공모주 청약이 있어요. 15~16일 양일간',
          arrivedDateTime: new Date('2025.07.01'),
          thumbnailUrl:
            'https://img.freepik.com/free-vector/illustration-south-korea-flag_53876-27132.jpg',
          expectedReadTime: '5분',
          isRead: true,
          newsletter: {
            category: '기술',
            name: 'UPPITY',
            imageUrl: 'https://example.com/newsletter-image.jpg',
          },
        }}
      />
    </ThemeProvider>
  );
};

const Container = styled.div`
  position: relative;
  z-index: 0;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  margin: 0;
  padding: 20px;

  background-color: #f0f0f0;
  font-family: Arial, sans-serif;
`;

export default App;
