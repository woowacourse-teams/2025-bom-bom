import styled from '@emotion/styled';
import Carousel from '@/components/Carousel/Carousel';

const SlideCardList = () => {
  return (
    <Carousel>
      <TrendContainer>
        <Title>인기 급상승 뉴스레터 🔥</Title>
        <Subtitle>
          지금 가장 많이 구독되고 있는 뉴스레터를 확인해보세요.
        </Subtitle>
      </TrendContainer>

      <ReadingKingContainer>
        <Title>이달의 독서왕 📰</Title>
        <Subtitle>지금 이달의 독서왕에 도전해보세요!</Subtitle>
      </ReadingKingContainer>

      <DiligenceContainer>
        <Title>이달의 습관왕 🏅</Title>
        <Subtitle>매일 뉴스레터를 읽고 이달의 습관왕에 도전해보세요!</Subtitle>
      </DiligenceContainer>
    </Carousel>
  );
};

export default SlideCardList;

const TrendContainer = styled.div`
  width: 100%;
  height: 100%;

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background: linear-gradient(135deg, #f96 0%, #ffb366 100%);
  text-align: center;
`;

const ReadingKingContainer = styled.div`
  width: 100%;
  height: 100%;

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background: linear-gradient(135deg, #ffb0a3 0%, #ffdedc 100%);
  text-align: center;
`;

const DiligenceContainer = styled.div`
  width: 100%;
  height: 100%;

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background: linear-gradient(135deg, #e97a82 0%, #c56d92 100%);
  text-align: center;
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.heading2};
`;

const Subtitle = styled.p`
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.heading5};
`;
