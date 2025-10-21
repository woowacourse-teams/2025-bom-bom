import styled from '@emotion/styled';
import Carousel from '@/components/Carousel/Carousel';

const banner1 = '/assets/avif/banner_1.avif';
const banner2 = '/assets/avif/banner_2.avif';
const banner3 = '/assets/avif/banner_3.avif';
const banner4 = '/assets/avif/banner_4.avif';

const BANNER_LABELS = [
  '디그, 디그와 함께 하세요. 누구보다 쉽게 핵심을 디깅해 드릴게요',
  '캐릿, 트렌드 당일 배송, 렛츠 캐릿',
  '데일리 바이트, 놓치면 안 될 비즈니스, 경제 이슈 한 눈에 보기',
  '부딩, 구독형 부동산 뉴스레터',
];

const SlideCardList = () => {
  return (
    <Carousel>
      <Banner src={banner1} alt={BANNER_LABELS[0]} />
      <Banner src={banner2} alt={BANNER_LABELS[1]} />
      <Banner src={banner3} alt={BANNER_LABELS[2]} />
      <Banner src={banner4} alt={BANNER_LABELS[3]} />
    </Carousel>
  );
};

export default SlideCardList;

const Banner = styled.img`
  width: 100%;
  height: 280px;

  object-fit: contain;
  object-position: center;
`;
