import styled from '@emotion/styled';
import Carousel from '@/components/Carousel/Carousel';

const banner1 = '/assets/avif/banner_1.avif';
const banner2 = '/assets/avif/banner_2.avif';
const banner3 = '/assets/avif/banner_3.avif';
const banner4 = '/assets/avif/banner_4.avif';

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
  height: 280px;

  background-image: url(${({ imageUrl }) => imageUrl});
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
`;
