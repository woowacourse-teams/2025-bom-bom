import { theme } from '@bombom/shared/theme';
import styled from '@emotion/styled';
import { Children } from 'react';
import { DEFAULT_SPEED } from './Carousel.constants';
import useCarousel from './useCarousel';
import ChevronIcon from '../icons/ChevronIcon';
import type { PropsWithChildren } from 'react';

type SlideButtonPosition = 'middle' | 'bottom';

type CarouselProps = PropsWithChildren & {
  hasSlideButton?: boolean;
  hasAnimation?: boolean;
  showNextSlidePart?: boolean;
} & (
    | { autoPlay?: true; autoPlaySpeedMs?: number }
    | { autoPlay: false; autoPlaySpeedMs?: never }
  ) &
  (
    | { hasSlideButton?: true; slideButtonPosition?: SlideButtonPosition }
    | { hasSlideButton: false; slideButtonPosition?: never }
  );

const Carousel = ({
  autoPlay = true,
  autoPlaySpeedMs = DEFAULT_SPEED,
  hasSlideButton = true,
  slideButtonPosition = 'middle',
  hasAnimation = true,
  showNextSlidePart = false,
  children,
}: CarouselProps) => {
  const originSlides = Children.toArray(children);
  const slideCount = originSlides.length;
  const hasMultipleSlides = slideCount > 1;

  if (process.env.NODE_ENV === 'development') {
    if (originSlides.length === 0) {
      throw new Error(
        'Carousel 컴포넌트에 최소 한 개 이상의 child가 필요합니다.',
      );
    }

    if (autoPlay && autoPlaySpeedMs < 100) {
      throw new Error('timer 주기는 100ms 이상이어야 합니다.');
    }

    if (autoPlay && originSlides.length === 1) {
      throw new Error('슬라이드가 1개일 경우, 자동 재생을 할 수 없습니다.');
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
    isSwiping,
    slideWrapperRef,
    handleTransitionEnd,
    handlePrevButtonClick,
    handleNextButtonClick,
    handleTouchStart,
    handleTouchMove,
    handleTouchEnd,
  } = useCarousel({
    slideCount,
    autoPlay: hasMultipleSlides ? autoPlay : false,
    autoPlaySpeedMs,
  });

  return (
    <Container slideButtonPosition={slideButtonPosition}>
      <SlidesWrapper
        ref={slideWrapperRef}
        slideIndex={slideIndex}
        isTransitioning={isTransitioning}
        isSwiping={isSwiping}
        onTransitionEnd={handleTransitionEnd}
        hasAnimation={hasMultipleSlides ? hasAnimation : false}
        showNextSlidePart={hasMultipleSlides ? showNextSlidePart : false}
        {...(hasMultipleSlides && {
          onTouchStart: handleTouchStart,
          onTouchMove: handleTouchMove,
          onTouchEnd: handleTouchEnd,
        })}
      >
        {infinitySlides.map((slideContent, index) => (
          <Slide key={`slide-${index}`}>{slideContent}</Slide>
        ))}
      </SlidesWrapper>

      {hasSlideButton && hasMultipleSlides && (
        <>
          <PrevSlideButton
            type="button"
            onClick={handlePrevButtonClick}
            slideButtonPosition={slideButtonPosition}
            aria-label="이전 슬라이드 이동"
          >
            <ChevronIcon
              direction="left"
              width="100%"
              height="100%"
              fill={theme.colors.primary}
            />
          </PrevSlideButton>

          <NextSlideButton
            type="button"
            onClick={handleNextButtonClick}
            slideButtonPosition={slideButtonPosition}
            aria-label="다음 슬라이드 이동"
          >
            <ChevronIcon
              direction="right"
              width="100%"
              height="100%"
              fill={theme.colors.primary}
            />
          </NextSlideButton>
        </>
      )}
    </Container>
  );
};

export default Carousel;

const TRANSITIONS = {
  slide: 'transform 0.3s ease-in-out',
  none: 'none',
} as const;

const Container = styled.div<{ slideButtonPosition?: SlideButtonPosition }>`
  overflow: hidden;
  position: relative;
  width: 100%;
  min-height: fit-content;
  padding: ${({ slideButtonPosition }) =>
    `0 12px ${slideButtonPosition === 'bottom' ? '60px' : '0'}`};

  background: transparent;
`;

const SlidesWrapper = styled.ul<{
  slideIndex: number;
  isTransitioning: boolean;
  isSwiping: boolean;
  hasAnimation: boolean;
  showNextSlidePart: boolean;
}>`
  position: relative;
  margin: ${({ showNextSlidePart }) =>
    showNextSlidePart ? '0 20px 0 -12px' : '0 -12px'};

  display: flex;

  transform: ${({ slideIndex }) => `translateX(-${slideIndex * 100}%)`};
  transition: ${({ hasAnimation, isTransitioning, isSwiping }) => {
    if (!hasAnimation || isSwiping) return TRANSITIONS.none;
    if (isTransitioning) return TRANSITIONS.slide;
    return TRANSITIONS.none;
  }};
`;

const Slide = styled.li`
  flex: 0 0 100%;
`;

const PrevSlideButton = styled.button<{
  slideButtonPosition: SlideButtonPosition;
}>`
  position: absolute;
  top: ${({ slideButtonPosition }) =>
    slideButtonPosition === 'middle' ? '50%' : 'auto'};
  bottom: ${({ slideButtonPosition }) =>
    slideButtonPosition === 'bottom' ? '8px' : 'auto'};
  left: 8px;
  width: clamp(32px, 10%, 48px);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgb(0 0 0 / 12%);

  display: flex;
  align-items: center;

  background-color: ${({ theme }) => theme.colors.white};

  transform: ${({ slideButtonPosition }) =>
    slideButtonPosition === 'middle' ? 'translateY(-50%)' : 'none'};

  &:hover {
    background-color: ${({ theme }) => theme.colors.dividers};
  }
`;

const NextSlideButton = styled.button<{
  slideButtonPosition: SlideButtonPosition;
}>`
  position: absolute;
  top: ${({ slideButtonPosition }) =>
    slideButtonPosition === 'middle' ? '50%' : 'auto'};
  right: 8px;
  bottom: ${({ slideButtonPosition }) =>
    slideButtonPosition === 'bottom' ? '8px' : 'auto'};
  width: clamp(32px, 10%, 48px);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgb(0 0 0 / 12%);

  display: flex;
  align-items: center;

  background-color: ${({ theme }) => theme.colors.white};

  transform: ${({ slideButtonPosition }) =>
    slideButtonPosition === 'middle' ? 'translateY(-50%)' : 'none'};

  &:hover {
    background-color: ${({ theme }) => theme.colors.dividers};
  }
`;
