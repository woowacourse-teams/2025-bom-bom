import styled from '@emotion/styled';
import ImageInfoCard from '@/components/ImageInfoCard/ImageInfoCard';
import type { Newsletter } from '@/types/newsletter';

interface NewsletterCardListProps {
  newsletters: Newsletter[];
  handleCardClick: (newsletter: Newsletter) => void;
}

const NewsletterCardList = ({
  newsletters,
  handleCardClick,
}: NewsletterCardListProps) => {
  return (
    <>
      {newsletters.map((newsletter) => (
        <NewsletterCard
          key={newsletter.newsletterId}
          imageUrl={newsletter.imageUrl ?? ''}
          title={newsletter.name}
          description={newsletter.description}
          onClick={() => handleCardClick(newsletter)}
          as="button"
          aria-label={`${newsletter.name} 뉴스레터 상세보기`}
        />
      ))}
    </>
  );
};

export default NewsletterCardList;

const NewsletterCard = styled(ImageInfoCard)`
  height: 80px;
  padding: 12px;
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
`;
