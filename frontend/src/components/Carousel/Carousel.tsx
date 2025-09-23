import styled from '@emotion/styled';
import { PropsWithChildren, Children } from 'react';
import useCarousel from './useCarousel';
import arrowNext from '#/assets/carousel-arrow-next.png';
import arrowPrev from '#/assets/carousel-arrow-prev.png';

interface CarouselProps extends PropsWithChildren {
  timer?: boolean | number;
  hasSlideButton?: boolean;
  hasAnimation?: boolean;
}

/**
 * @property {boolean|number} [timer=true] - 자동 슬라이드 재생 여부 또는 주기 설정(ms).
 *   - `false`: 비활성화
 *   - `true`: 기본 4초 주기
 *   - `number`: 커스텀 주기(ms)
 * @property {boolean} [hasSlideButton=true] - 슬라이드 변경 버튼 렌더링 여부
 * @property {boolean} [hasAnimation=true] - 애니메이션 타입
 *   - `slide`: 왼쪽에서 오른쪽으로 미끄러지는 애니메이션
 *   - `none`: 애니메이션 없음
 * @property {React.ReactNode} children - 슬라이드로 렌더링할 자식 요소들
 */
const Carousel = ({
  timer = true,
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

    if (typeof timer === 'number' && timer < 100) {
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
  } = useCarousel({ slideCount, timer });

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
  margin: 0 auto 20px;

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
