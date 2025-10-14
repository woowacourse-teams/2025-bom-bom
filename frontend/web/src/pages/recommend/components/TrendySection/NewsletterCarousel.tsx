import NewsletterCardList from './NewsletterCardList';
import Carousel from '@/components/Carousel/Carousel';
import { chunk } from '@/utils/array';
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
  const newsletterSlides = chunk(newsletters, ITEMS_PER_SLIDE);
  return (
    <>
      {newsletterSlides.length === 1 ? (
        <NewsletterCardList
          newsletters={newsletters}
          handleCardClick={handleCardClick}
        />
      ) : (
        <Carousel
          autoPlay={false}
          hasSlideButton={false}
          showNextSlidePart={true}
        >
          {newsletterSlides.map((newslettersOfSlide, slideIndex) => (
            <NewsletterCardList
              key={`newsletters-${slideIndex}`}
              newsletters={newslettersOfSlide}
              handleCardClick={handleCardClick}
            />
          ))}
        </Carousel>
      )}
    </>
  );
};

export default NewsletterCarousel;
