import styled from '@emotion/styled';
import Carousel from '@/components/Carousel/Carousel';

const banner1 = '/assets/banner_1.png';
const banner2 = '/assets/banner_2.png';
const banner3 = '/assets/banner_3.png';
const banner4 = '/assets/banner_4.png';

const SlideCardList = () => {
  return (
    <Carousel>
      <Banner imageUrl={banner1} />
      <Banner imageUrl={banner2} />
      <Banner imageUrl={banner3} />
      <Banner imageUrl={banner4} />
    </Carousel>
  );
};

export default SlideCardList;

const Banner = styled.div<{ imageUrl: string }>`
  width: 100%;
  height: 100%;

  background-image: url(${({ imageUrl }) => imageUrl});
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;

  /* aspect-ratio: 16 / 9; */
`;

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
