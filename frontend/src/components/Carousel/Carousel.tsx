import styled from '@emotion/styled';
import {
  PropsWithChildren,
  useState,
  useRef,
  Children,
  useEffect,
} from 'react';
import arrowNext from '#/assets/carousel-arrow-next.png';
import arrowPrev from '#/assets/carousel-arrow-prev.png';

interface CarouselProps {
  timer?: boolean | number;
  children: React.ReactNode;
}
interface CarouselProps extends PropsWithChildren {
  timer?: boolean | number;
}

const DEFAULT_DELAY = 4000;
const START_SLIDE_INDEX = 1;

/**
 * @property {boolean|number} [timer=true] - 자동 슬라이드 재생 여부 또는 주기 설정(ms).
 *   - `false`: 비활성화
 *   - `true`: 기본 4초 주기
 *   - `number`: 커스텀 주기(ms)
 * @property {React.ReactNode} children - 슬라이드로 렌더링할 자식 요소들
 */
const Carousel = ({ timer = true, children }: CarouselProps) => {
  const originSlides = Children.toArray(children);
  const [slideIndex, setSlideIndex] = useState(START_SLIDE_INDEX);
  const [isTransitioning, setIsTransitioning] = useState(false);
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  if (process.env.NODE_ENV === 'development') {
    if (originSlides.length === 0) {
      throw new Error(
        'Carousel 컴포넌트에 최소 한 개 이상의 child가 필요합니다.',
      );
    }

    if (typeof timer === 'number' && timer < 100) {
      throw new Error('timer 주기는 100ms 이상이어야 합니다.');
    }
  }

  const infinitySlides = [
    originSlides[originSlides.length - 1],
    ...originSlides,
    originSlides[0],
  ];

  const handleTransitionEnd = () => {
    setIsTransitioning(false);

    if (slideIndex <= 0) {
      setSlideIndex(originSlides.length);
    }

    if (slideIndex >= infinitySlides.length - 1) {
      setSlideIndex(START_SLIDE_INDEX);
    }
  };

  useEffect(() => {
    if (!timer) return;

    const autoSlideDelay = typeof timer === 'number' ? timer : DEFAULT_DELAY;
    if (!isTransitioning) {
      timerIdRef.current = setTimeout(() => {
        setIsTransitioning(true);
        setSlideIndex((prev) => prev + 1);
      }, autoSlideDelay);
    }

    return () => {
      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }
    };
  }, [timer, slideIndex, isTransitioning]);

  const handlePrevButtonClick = () => {
    if (isTransitioning) return;

    setIsTransitioning(true);
    setSlideIndex((prev) => prev - 1);
  };

  const handleNextButtonClick = () => {
    if (isTransitioning) return;

    setIsTransitioning(true);
    setSlideIndex((prev) => prev + 1);
  };

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
