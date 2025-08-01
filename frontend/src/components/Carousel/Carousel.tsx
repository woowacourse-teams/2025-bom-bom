import styled from '@emotion/styled';
import React, {
  PropsWithChildren,
  useState,
  useRef,
  useCallback,
  useEffect,
} from 'react';
import arrowNext from '#/assets/carousel-arrow-next.png';
import arrowPrev from '#/assets/carousel-arrow-prev.png';

const Carousel = ({ children }: PropsWithChildren) => {
  const [slideIndex, setSlideIndex] = useState(0);
  const [slideWidth, setSlideWidth] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);
  const totalSlides = React.Children.count(children);

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

  const handlePrevButtonClick = () => {
    setSlideIndex((prev) => (prev > 0 ? prev - 1 : totalSlides - 1));
  };

  const handleNextButtonClick = () => {
    setSlideIndex((prev) => (prev < totalSlides - 1 ? prev + 1 : 0));
  };

  return (
    <Container ref={containerRef}>
      <SlidesWrapper slideIndex={slideIndex} slideWidth={slideWidth}>
        {React.Children.map(children, (child, index) => (
          <Slide key={`slide-${index}`} slideWidth={slideWidth}>
            {child}
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

  background: transparent;
`;

const SlidesWrapper = styled.ul<{ slideIndex: number; slideWidth: number }>`
  height: 100%;

  display: flex;

  transform: ${({ slideIndex, slideWidth }) =>
    `translateX(-${slideIndex * slideWidth}px)`};
  transition: transform 0.3s ease-in-out;
`;

const Slide = styled.li<{ slideWidth: number }>`
  flex: 0 0 ${({ slideWidth }) => slideWidth}px;
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
