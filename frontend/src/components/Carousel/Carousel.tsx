import styled from '@emotion/styled';
import {
  PropsWithChildren,
  useState,
  useRef,
  useCallback,
  Children,
} from 'react';
import { useThrottle } from '@/hooks/useThrottle';
import arrowNext from '#/assets/carousel-arrow-next.png';
import arrowPrev from '#/assets/carousel-arrow-prev.png';

interface CarouselProps extends PropsWithChildren {
  timer?: boolean;
}

const START_SLIDE_INDEX = 1;

const Carousel = ({ timer = true, children }: CarouselProps) => {
  const originSlides = Children.toArray(children);
  const [slideIndex, setSlideIndex] = useState(START_SLIDE_INDEX);
  const [isTransitioning, setIsTransitioning] = useState(false);
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  if (originSlides.length === 0 && process.env.NODE_ENV === 'development') {
    throw new Error(
      'Carousel 컴포넌트에 최소 한 개 이상의 child가 필요합니다.',
    );
  }

  const infinitySlides = [
    originSlides[originSlides.length - 1],
    ...originSlides,
    originSlides[0],
  ];

  const handleTransitionEnd = () => {
    setIsTransitioning(false);

    if (slideIndex === 0) {
      setSlideIndex(originSlides.length);
    }

    if (slideIndex === infinitySlides.length - 1) {
      setSlideIndex(START_SLIDE_INDEX);
    }
  };

  const setSlidingTimer = useCallback(() => {
    if (timerIdRef.current) {
      clearTimeout(timerIdRef.current);
    }

    timerIdRef.current = setTimeout(() => {
      setIsTransitioning(true);
      setSlideIndex((prev) => prev + 1);
    }, 4000);
  }, []);

  const handlePrevButtonClick = useThrottle(() => {
    setIsTransitioning(true);
    setSlideIndex((prev) => prev - 1);
  }, 500);

  const handleNextButtonClick = useThrottle(() => {
    setIsTransitioning(true);
    setSlideIndex((prev) => prev + 1);
  }, 500);

  if (timer) {
    setSlidingTimer();
  }

  return (
    <Container>
      <SlidesWrapper
        slideIndex={slideIndex}
        isTransitioning={isTransitioning}
        onTransitionEnd={handleTransitionEnd}
      >
        {infinitySlides.map((slideContent, index) => (
          <Slide key={`slide-${index}`}>{slideContent}</Slide>
        ))}
      </SlidesWrapper>

      <PrevSlideButton type="button" onClick={handlePrevButtonClick}>
        <img src={arrowPrev} alt="이전 슬라이드 버튼" />
      </PrevSlideButton>

      <NextSlideButton type="button" onClick={handleNextButtonClick}>
        <img src={arrowNext} alt="다음 슬라이드 버튼" />
      </NextSlideButton>
    </Container>
  );
};

export default Carousel;

const Container = styled.div`
  overflow: hidden;
  position: relative;
  width: 100%;
  height: 320px;
  margin: 0 auto 20px;
  border-radius: 16px;

  background: transparent;
`;

const SlidesWrapper = styled.ul<{
  slideIndex: number;
  isTransitioning: boolean;
}>`
  height: 100%;

  display: flex;

  transform: ${({ slideIndex }) => `translateX(-${slideIndex * 100}%)`};
  transition: ${({ isTransitioning }) =>
    isTransitioning ? 'transform 0.3s ease-in-out' : 'none'};
`;

const Slide = styled.li`
  height: 100%;
  flex: 0 0 100%;
`;

const PrevSlideButton = styled.button`
  position: absolute;
  top: 50%;
  left: 24px;

  transform: translateY(-50%);

  &:hover {
    opacity: 0.8;
  }
`;

const NextSlideButton = styled.button`
  position: absolute;
  top: 50%;
  right: 24px;

  transform: translateY(-50%);

  &:hover {
    opacity: 0.8;
  }
`;
