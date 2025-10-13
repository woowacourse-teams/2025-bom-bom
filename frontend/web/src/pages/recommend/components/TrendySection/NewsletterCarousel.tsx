import styled from '@emotion/styled';
import NewsletterCardList from './NewsletterCardList';
import Carousel from '@/components/Carousel/Carousel';
import type { Newsletter } from '@/types/newsletter';

const ITEMS_PER_SLIDE = 5;

interface NewsletterCarouselProps {
  newsletters: Newsletter[];
  handleCardClick: (newsletter: Newsletter) => void;
}

const NewsletterCarousel = ({
  newsletters,
  handleCardClick,
}: NewsletterCarouselProps) => {
  const createNewsletterSlides = (
    newsletters: Newsletter[],
    size: number,
  ): Newsletter[][] => {
    const result: Newsletter[][] = Array.from(
      { length: Math.ceil(newsletters.length / size) },
      () => [],
    );

    newsletters.forEach((newsletter, index) => {
      const splittedIndex = Math.floor(index / size);
      result[splittedIndex]?.push(newsletter);
    });

    return result;
  };

  const newsletterSlides = createNewsletterSlides(newsletters, ITEMS_PER_SLIDE);
  return (
    <>
      {newsletterSlides.length === 1 ? (
        <SlideNewsletters>
          <NewsletterCardList
            newsletters={newsletters}
            handleCardClick={handleCardClick}
          />
        </SlideNewsletters>
      ) : (
        <Carousel
          autoPlay={false}
          hasSlideButton={false}
          showNextSlidePart={true}
        >
          {newsletterSlides.map((newslettersOfSlide, slideIndex) => (
            <SlideNewsletters key={`newsletters-${slideIndex}`}>
              <NewsletterCardList
                newsletters={newslettersOfSlide}
                handleCardClick={handleCardClick}
              />
            </SlideNewsletters>
          ))}
        </Carousel>
      )}
    </>
  );
};

export default NewsletterCarousel;

const SlideNewsletters = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;
