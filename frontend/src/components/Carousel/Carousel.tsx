import styled from '@emotion/styled';
import React, {
  PropsWithChildren,
  useState,
  useRef,
  useCallback,
  useEffect,
} from 'react';
import { useThrottle } from '@/hooks/useThrottle';
import arrowNext from '#/assets/carousel-arrow-next.png';
import arrowPrev from '#/assets/carousel-arrow-prev.png';

interface CarouselProps extends PropsWithChildren {
  timer?: boolean;
}

const START_SLIDE_INDEX = 1;
const INIT_SLIDE_WIDTH = 0;

const Carousel = ({ timer = true, children }: CarouselProps) => {
  const originSlides = React.Children.toArray(children);
  const [slideIndex, setSlideIndex] = useState(START_SLIDE_INDEX);
  const [slideWidth, setSlideWidth] = useState(INIT_SLIDE_WIDTH);
  const [isTransitioning, setIsTransitioning] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const infinitySlides = [
    originSlides[originSlides.length - 1],
    ...originSlides,
    originSlides[0],
  ];

  const updateWidth = useCallback(() => {
    if (containerRef.current) {
      setSlideWidth(containerRef.current.clientWidth);
    }
  }, []);

  useEffect(() => {
    if (!containerRef.current) return;

    const resizeObserver = new ResizeObserver(() => {
      updateWidth();
    });
    resizeObserver.observe(containerRef.current);
    updateWidth();

    return () => {
      resizeObserver.disconnect();
    };
  }, [updateWidth]);

  const handleTransitionEnd = () => {
    setIsTransitioning(false);

    if (slideIndex === 0) {
      setSlideIndex(originSlides.length);
    }

    if (slideIndex === infinitySlides.length - 1) {
      setSlideIndex(1);
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
    <Container ref={containerRef}>
      <SlidesWrapper
        slideIndex={slideIndex}
        slideWidth={slideWidth}
        isTransitioning={isTransitioning}
        onTransitionEnd={handleTransitionEnd}
      >
        {infinitySlides.map((slideContent, index) => (
          <Slide key={`slide-${index}`} slideWidth={slideWidth}>
            {slideContent}
          </Slide>
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
  height: 330px;
  margin: 0 auto 18px;
  border-radius: 16px;

  background: transparent;
`;

const SlidesWrapper = styled.ul<{
  slideIndex: number;
  slideWidth: number;
  isTransitioning: boolean;
}>`
  height: 100%;

  display: flex;

  transform: ${({ slideIndex }) => `translateX(-${slideIndex * 100}%)`};
  transition: ${({ isTransitioning }) =>
    isTransitioning ? 'transform 0.3s ease-in-out' : 'none'};
`;

const Slide = styled.li<{ slideWidth: number }>`
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
