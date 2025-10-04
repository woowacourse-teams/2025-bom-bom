import { useCallback, useEffect, useRef, useState } from 'react';
import {
  DEFAULT_SPEED,
  START_SLIDE_INDEX,
  SWIPE_OFFSET_THRESHOLD,
} from './Carousel.constants';
import type { TouchEvent } from 'react';

interface UseCarouselProps {
  slideCount: number;
  autoPlay?: boolean;
  autoPlaySpeedMs?: number;
}

const useCarousel = ({
  slideCount,
  autoPlay = true,
  autoPlaySpeedMs = DEFAULT_SPEED,
}: UseCarouselProps) => {
  const [slideIndex, setSlideIndex] = useState(START_SLIDE_INDEX);
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

  const handleTransitionEnd = () => {
    setIsTransitioning(false);

    if (slideIndex < START_SLIDE_INDEX) {
      setSlideIndex(slideCount);
    }

    if (slideIndex > slideCount) {
      setSlideIndex(START_SLIDE_INDEX);
    }
  };

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

  const handlePrevButtonClick = () => {
    if (isTransitioning || isSwiping) return;

    setIsTransitioning(true);
    setSlideIndex((prev) => prev - 1);
  };

  const handleNextButtonClick = () => {
    if (isTransitioning || isSwiping) return;

    setIsTransitioning(true);
    setSlideIndex((prev) => prev + 1);
  };

  const swipeStart = (clientX: number) => {
    if (isTransitioning) return;

    setIsSwiping(true);
    swipeStartRef.current = clientX;
    swipeOffsetRef.current = 0;
  };

  const swipeMove = (clientX: number) => {
    if (!isSwiping) return;

    const offset = clientX - swipeStartRef.current;
    swipeOffsetRef.current = offset;
    updateTransform();
  };

  const swipeEnd = () => {
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
  };

  const handleTouchStart = (e: TouchEvent) => {
    const touchPoint = e.touches[0];
    if (!touchPoint) return;
    swipeStart(touchPoint.clientX);
  };

  const handleTouchMove = (e: TouchEvent) => {
    const touchPoint = e.touches[0];
    if (!touchPoint) return;
    swipeMove(touchPoint.clientX);
  };

  const handleTouchEnd = () => {
    swipeEnd();
  };

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
