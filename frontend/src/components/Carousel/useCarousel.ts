import { useEffect, useRef, useState } from 'react';

interface UseCarouselProps {
  slideCount: number;
  timer?: boolean | number;
}

const DEFAULT_DELAY = 4000;
const START_SLIDE_INDEX = 1;

const useCarousel = ({ slideCount, timer = true }: UseCarouselProps) => {
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

  return {
    slideIndex,
    isTransitioning,
    handleTransitionEnd,
    handlePrevButtonClick,
    handleNextButtonClick,
  };
};

export default useCarousel;
