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
`;
