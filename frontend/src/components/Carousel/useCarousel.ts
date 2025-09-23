import { useEffect, useRef, useState } from 'react';
import { DEFAULT_DELAY, START_SLIDE_INDEX } from './Carousel.constants';

interface UseCarouselProps {
  slideCount: number;
  autoPlay?: boolean;
  autoPlaySpeedMs?: number;
}

const useCarousel = ({
  slideCount,
  autoPlay = true,
  autoPlaySpeedMs = DEFAULT_DELAY,
}: UseCarouselProps) => {
  const [slideIndex, setSlideIndex] = useState(START_SLIDE_INDEX);
  const [isTransitioning, setIsTransitioning] = useState(false);
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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
    if (!autoPlay) return;

    const autoSlideDelay = autoPlaySpeedMs ?? DEFAULT_DELAY;
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
  }, [autoPlay, slideIndex, isTransitioning, autoPlaySpeedMs]);

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

  return {
    slideIndex,
    isTransitioning,
    handleTransitionEnd,
    handlePrevButtonClick,
    handleNextButtonClick,
  };
};

export default useCarousel;
