import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { createPortal } from 'react-dom';
import NewsletterDetail from '../NewsletterDetail/NewsletterDetail';
import { queries } from '@/apis/queries';
import Chip from '@/components/Chip/Chip';
import ImageInfoCard from '@/components/ImageInfoCard/ImageInfoCard';
import Modal from '@/components/Modal/Modal';
import useModal from '@/components/Modal/useModal';
import { CATEGORIES } from '@/constants/category';
import { useDevice } from '@/hooks/useDevice';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import type { Category } from '@/constants/category';
import type { Device } from '@/hooks/useDevice';
import type { Newsletter } from '@/types/newsletter';
import TrendingUpIcon from '#/assets/svg/trending-up.svg';

const TrendySection = () => {
  const [selectedCategory, setSelectedCategory] = useState<Category>('전체');
  const [selectedNewsletter, setSelectedNewsletter] =
    useState<Newsletter | null>(null);

  const { data: newsletters } = useQuery(queries.newsletters());
  const {
    modalRef: detailModalRef,
    openModal: openDetailModal,
    closeModal: closeDetailModal,
    isOpen,
  } = useModal();
  const device = useDevice();

  if (!newsletters) return null;

  const filteredNewsletters = newsletters.filter(
    (newsletter) =>
      selectedCategory === '전체' || newsletter.category === selectedCategory,
  );

  const handleCardClick = (newsletter: Newsletter) => {
    setSelectedNewsletter(newsletter);
    openDetailModal();

    trackEvent({
      category: 'Newsletter',
      action: 'Click Trendy Newsletter Card',
      label: newsletter.name ?? 'Unknown Newsletter',
    });
  };

  return (
    <>
      <Container>
        <SectionHeader>
          <SectionIconBox>
            <TrendingUpIcon width={16} height={16} />
          </SectionIconBox>
          <SectionTitle>트렌디한 뉴스레터</SectionTitle>
        </SectionHeader>
        <TagContainer>
          {CATEGORIES.map((category, index) => (
            <Chip
              key={index}
              text={category}
              selected={selectedCategory === category}
              onSelect={() => setSelectedCategory(category)}
            />
          ))}
        </TagContainer>
        <TrendyGrid device={device}>
          {filteredNewsletters.map((newsletter) => (
            <NewsletterCard
              key={newsletter.newsletterId}
              imageUrl={newsletter.imageUrl ?? ''}
              title={newsletter.name}
              description={newsletter.description}
              onClick={() => handleCardClick(newsletter)}
              as="button"
            />
          ))}
        </TrendyGrid>
      </Container>
      {createPortal(
        <Modal
          modalRef={detailModalRef}
          closeModal={closeDetailModal}
          isOpen={isOpen}
          position={device === 'mobile' ? 'bottom' : 'center'}
          showCloseButton={device !== 'mobile'}
        >
          {selectedNewsletter && (
            <NewsletterDetail
              newsletterId={selectedNewsletter.newsletterId}
              category={selectedNewsletter.category}
            />
          )}
        </Modal>,
        document.body,
      )}
    </>
  );
};

export default TrendySection;

const Container = styled.div`
  width: 100%;
  padding: 24px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 20px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  background: ${({ theme }) => theme.colors.white};

  backdrop-filter: blur(10px);
`;

const SectionHeader = styled.div`
  margin-bottom: 16px;

  display: flex;
  gap: 8px;
  align-items: center;
`;

const SectionIconBox = styled.span`
  width: 28px;
  height: 28px;
  border-radius: 12px;

  display: flex;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const SectionTitle = styled.h2`
  margin: 0;

  color: ${({ theme }) => theme.colors.black};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const TagContainer = styled.div`
  margin-bottom: 16px;

  display: flex;
  gap: 8px;
  flex-wrap: wrap;
`;

const TrendyGrid = styled.div<{ device: Device }>`
  display: grid;
  grid-template-columns: ${({ device }) =>
    device === 'mobile' ? '1fr' : 'repeat(2, 1fr)'};
`;

const NewsletterCard = styled(ImageInfoCard)`
  padding: 8px;
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
