import { useCallback, useEffect, useRef, useState } from 'react';
import {
  DEFAULT_SPEED,
  INFINITY_START_SLIDE_INDEX,
  FINITE_START_SLIDE_INDEX,
  SWIPE_OFFSET_THRESHOLD,
} from './Carousel.constants';
import type { TouchEvent } from 'react';

interface UseCarouselProps {
  slideCount: number;
  isInfinity?: boolean;
  autoPlay?: boolean;
  autoPlaySpeedMs?: number;
}

const useCarousel = ({
  slideCount,
  isInfinity = true,
  autoPlay = true,
  autoPlaySpeedMs = DEFAULT_SPEED,
}: UseCarouselProps) => {
  const [slideIndex, setSlideIndex] = useState(
    isInfinity ? INFINITY_START_SLIDE_INDEX : FINITE_START_SLIDE_INDEX,
  );
  const [isTransitioning, setIsTransitioning] = useState(false);
  const [isSwiping, setIsSwiping] = useState(false);
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const swipeOffsetRef = useRef(0);
  const swipeStartRef = useRef(0);
  const slideWrapperRef = useRef<HTMLUListElement>(null);

  const updateTransform = useCallback(() => {
    if (!slideWrapperRef.current) return;

    const offset = swipeOffsetRef.current;
    slideWrapperRef.current.style.transform = `translateX(calc(-${slideIndex * 100}% + ${offset}px))`;
  }, [slideIndex]);

  const handleTransitionEnd = useCallback(() => {
    setIsTransitioning(false);

    if (!isInfinity) return;

    if (slideIndex < INFINITY_START_SLIDE_INDEX) {
      setSlideIndex(slideCount);
    }

    if (slideIndex > slideCount) {
      setSlideIndex(INFINITY_START_SLIDE_INDEX);
    }
  }, [slideIndex, slideCount, isInfinity]);

  useEffect(() => {
    if (!autoPlay || isSwiping) return;

    const autoSlideDelay = autoPlaySpeedMs ?? DEFAULT_SPEED;
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
  }, [autoPlay, slideIndex, isTransitioning, autoPlaySpeedMs, isSwiping]);

  const handlePrevButtonClick = useCallback(() => {
    if (isTransitioning || isSwiping) return;
    if (!isInfinity && slideIndex <= 0) return;

    setIsTransitioning(true);
    setSlideIndex((prev) => prev - 1);
  }, [isTransitioning, isSwiping, isInfinity, slideIndex]);

  const handleNextButtonClick = useCallback(() => {
    if (isTransitioning || isSwiping) return;
    if (!isInfinity && slideIndex >= slideCount - 1) return;

    setIsTransitioning(true);
    setSlideIndex((prev) => prev + 1);
  }, [isTransitioning, isSwiping, isInfinity, slideIndex, slideCount]);

  const swipeStart = useCallback(
    (clientX: number) => {
      if (isTransitioning) return;

      setIsSwiping(true);
      swipeStartRef.current = clientX;
      swipeOffsetRef.current = 0;
    },
    [isTransitioning],
  );
  const swipeMove = useCallback(
    (clientX: number) => {
      if (!isSwiping) return;

      const offset = clientX - swipeStartRef.current;
      const isFirstSlide = slideIndex === FINITE_START_SLIDE_INDEX;
      const isLastSlide = slideIndex === slideCount - 1;

      const isSwipeBoundary =
        !isInfinity &&
        ((offset > 0 && isFirstSlide) || (offset < 0 && isLastSlide));

      swipeOffsetRef.current = isSwipeBoundary ? 0 : offset;
      updateTransform();
    },
    [isInfinity, isSwiping, slideCount, slideIndex, updateTransform],
  );

  const swipeEnd = useCallback(() => {
    if (!isSwiping) return;

    setIsSwiping(false);

    if (Math.abs(swipeOffsetRef.current) >= SWIPE_OFFSET_THRESHOLD) {
      setIsTransitioning(true);
      const swipeIndex = swipeOffsetRef.current > 0 ? -1 : 1;
      setSlideIndex((prev) => prev + swipeIndex);
    }

    swipeOffsetRef.current = 0;
    if (slideWrapperRef.current) {
      slideWrapperRef.current.style.transform = '';
    }
  }, [isSwiping]);

  const handleTouchStart = useCallback(
    (e: TouchEvent) => {
      const touchPoint = e.touches[0];
      if (!touchPoint) return;
      swipeStart(touchPoint.clientX);
    },
    [swipeStart],
  );

  const handleTouchMove = useCallback(
    (e: TouchEvent) => {
      const touchPoint = e.touches[0];
      if (!touchPoint) return;
      swipeMove(touchPoint.clientX);
    },
    [swipeMove],
  );

  const handleTouchEnd = useCallback(() => {
    swipeEnd();
  }, [swipeEnd]);

  return {
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
  };
};

export default useCarousel;
