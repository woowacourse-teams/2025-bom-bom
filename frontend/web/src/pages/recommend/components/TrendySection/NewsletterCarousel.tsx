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
  return (
    <Carousel hasSlideButton={false} showNextSlidePart={true}>
      {chunk(newsletters, ITEMS_PER_SLIDE).map(
        (newslettersOfSlide, slideIndex) => (
          <NewsletterCardList
            key={`newsletters-${slideIndex}`}
            newsletters={newslettersOfSlide}
            handleCardClick={handleCardClick}
          />
        ),
      )}
    </Carousel>
  );
};

export default NewsletterCarousel;
