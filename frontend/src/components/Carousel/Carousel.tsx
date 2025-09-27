import styled from '@emotion/styled';
import { Children } from 'react';
import { DEFAULT_SPEED } from './Carousel.constants';
import useCarousel from './useCarousel';
import type { PropsWithChildren } from 'react';
import arrowNext from '#/assets/avif/carousel-arrow-next.avif';
import arrowPrev from '#/assets/avif/carousel-arrow-prev.avif';

type CarouselProps = PropsWithChildren & {
  hasSlideButton?: boolean;
  hasAnimation?: boolean;
} & (
    | { autoPlay?: true; autoPlaySpeedMs?: number }
    | { autoPlay: false; autoPlaySpeedMs?: never }
  );

const Carousel = ({
  autoPlay = true,
  autoPlaySpeedMs = DEFAULT_SPEED,
  hasSlideButton = true,
  hasAnimation = true,
  children,
}: CarouselProps) => {
  const originSlides = Children.toArray(children);
  const slideCount = originSlides.length;

  if (process.env.NODE_ENV === 'development') {
    if (originSlides.length === 0) {
      throw new Error(
        'Carousel 컴포넌트에 최소 한 개 이상의 child가 필요합니다.',
      );
    }

    if (autoPlay && autoPlaySpeedMs < 100) {
      throw new Error('timer 주기는 100ms 이상이어야 합니다.');
    }
  }

  const infinitySlides = [
    originSlides[originSlides.length - 1],
    ...originSlides,
    originSlides[0],
  ];

  const {
    slideIndex,
    isTransitioning,
    handleTransitionEnd,
    handlePrevButtonClick,
    handleNextButtonClick,
  } = useCarousel({ slideCount, autoPlay, autoPlaySpeedMs });

  return (
    <Container>
      <SlidesWrapper
        slideIndex={slideIndex}
        isTransitioning={isTransitioning}
        onTransitionEnd={handleTransitionEnd}
        hasAnimation={hasAnimation}
      >
        {infinitySlides.map((slideContent, index) => (
          <Slide key={`slide-${index}`}>{slideContent}</Slide>
        ))}
      </SlidesWrapper>

      {hasSlideButton && (
        <>
          <PrevSlideButton type="button" onClick={handlePrevButtonClick}>
            <img src={arrowPrev} alt="이전 슬라이드 버튼" />
          </PrevSlideButton>

          <NextSlideButton type="button" onClick={handleNextButtonClick}>
            <img src={arrowNext} alt="다음 슬라이드 버튼" />
          </NextSlideButton>
        </>
      )}
    </Container>
  );
};

export default Carousel;

const TRANSITIONS = {
  slide: 'transform 0.3s ease-in-out',
  none: 'transform 0.3s step-end',
} as const;

const Container = styled.div`
  overflow: hidden;
  position: relative;
  width: 100%;
  min-height: fit-content;
  margin: 0 auto;

  background: transparent;
`;

const SlidesWrapper = styled.ul<{
  slideIndex: number;
  isTransitioning: boolean;
  hasAnimation: boolean;
}>`
  position: relative;

  display: flex;

  transform: ${({ slideIndex }) => `translateX(-${slideIndex * 100}%)`};
  transition: ${({ hasAnimation, isTransitioning }) =>
    hasAnimation
      ? isTransitioning
        ? TRANSITIONS.slide
        : TRANSITIONS.none
      : TRANSITIONS.none};
`;

const Slide = styled.li`
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
