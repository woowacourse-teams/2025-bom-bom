import styled from '@emotion/styled';
import Carousel from '@/components/Carousel/Carousel';
import ImageInfoCard from '@/components/ImageInfoCard/ImageInfoCard';
import ImageInfoCardSkeleton from '@/components/ImageInfoCard/ImageInfoCardSkeleton';
import { NEWSLETTER_COUNT } from '@/constants/newsletter';
import { useDevice, type Device } from '@/hooks/useDevice';
import type { Newsletter } from '@/types/newsletter';

const ITEMS_PER_SLIDE = 5;

interface NewsletterListProps {
  newsletters: Newsletter[];
  isLoading: boolean;
  handleCardClick: (newsletter: Newsletter) => void;
}

const NewsletterList = ({
  newsletters,
  isLoading,
  handleCardClick,
}: NewsletterListProps) => {
  const device = useDevice();

  const splitNewsletters = (
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

  if (isLoading) {
    return (
      <TrendyGrid device={device}>
        {Array.from({
          length:
            device === 'mobile'
              ? NEWSLETTER_COUNT.mobile
              : NEWSLETTER_COUNT.nonMobile,
        }).map((_, index) => (
          <ImageInfoCardSkeleton key={`skeleton-card-${index}`} />
        ))}
      </TrendyGrid>
    );
  }

  const splittedNewsletters = splitNewsletters(newsletters, ITEMS_PER_SLIDE);

  return (
    <TrendyGrid device={device}>
      {newsletters.length === 0 ? (
        <p>해당 카테고리에 뉴스레터가 없습니다.</p>
      ) : device === 'mobile' ? (
        splittedNewsletters.length === 1 ? (
          <SlideNewsletters>
            {newsletters.map((newsletter) => (
              <NewsletterCard
                key={newsletter.newsletterId}
                imageUrl={newsletter.imageUrl ?? ''}
                title={newsletter.name}
                description={newsletter.description}
                onClick={() => handleCardClick(newsletter)}
                as="button"
              />
            ))}
          </SlideNewsletters>
        ) : (
          <Carousel
            autoPlay={false}
            hasSlideButton={false}
            showNextSlidePart={true}
          >
            {splittedNewsletters.map((newsletterItems, slideIndex) => (
              <SlideNewsletters key={`newsletters-${slideIndex}`}>
                {newsletterItems.map((newsletter) => (
                  <NewsletterCard
                    key={newsletter.newsletterId}
                    imageUrl={newsletter.imageUrl ?? ''}
                    title={newsletter.name}
                    description={newsletter.description}
                    onClick={() => handleCardClick(newsletter)}
                    as="button"
                  />
                ))}
              </SlideNewsletters>
            ))}
          </Carousel>
        )
      ) : (
        newsletters.map((newsletter) => (
          <NewsletterCard
            key={newsletter.newsletterId}
            imageUrl={newsletter.imageUrl ?? ''}
            title={newsletter.name}
            description={newsletter.description}
            onClick={() => handleCardClick(newsletter)}
            as="button"
          />
        ))
      )}
    </TrendyGrid>
  );
};

export default NewsletterList;

const SlideNewsletters = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const TrendyGrid = styled.div<{ device: Device }>`
  display: grid;
  gap: 12px;

  grid-template-columns: ${({ device }) =>
    device === 'mobile' ? '1fr' : 'repeat(2, 1fr)'};
`;

const NewsletterCard = styled(ImageInfoCard)`
  border-radius: 16px;

  cursor: pointer;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 8px 25px -8px rgb(0 0 0 / 12%);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(-1px);
  }

  &:focus-visible {
    outline: none;
  }

  &:focus:not(:focus-visible) {
    outline: none;
  }
`;
