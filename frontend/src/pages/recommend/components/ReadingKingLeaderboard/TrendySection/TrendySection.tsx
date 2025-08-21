import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { createPortal } from 'react-dom';
import NewsletterDetail from '../../NewsletterDetail/NewsletterDetail';
import { queries } from '@/apis/queries';
import Chip from '@/components/Chip/Chip';
import ImageInfoCard from '@/components/ImageInfoCard/ImageInfoCard';
import Modal from '@/components/Modal/Modal';
import useModal from '@/components/Modal/useModal';
import { CATEGORIES, CategoryType } from '@/constants/category';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { Newsletter } from '@/types/newsletter';
import TrendingUpIcon from '#/assets/trending-up.svg';

const TrendySection = () => {
  const [selectedCategory, setSelectedCategory] =
    useState<CategoryType>('전체');
  const [selectedNewsletter, setSelectedNewsletter] =
    useState<Newsletter | null>(null);

  const { data: newsletters } = useQuery(queries.newsletters());
  const {
    modalRef: detailModalRef,
    openModal: openDetailModal,
    closeModal: closeDetailModal,
    isOpen,
  } = useModal();
  const deviceType = useDeviceType();

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
        <TrendyGrid deviceType={deviceType}>
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
          position={deviceType === 'mobile' ? 'bottom' : 'center'}
          showCloseButton={deviceType !== 'mobile'}
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
  padding: 28px;
  border: 1px solid ${({ theme }) => theme.colors.dividers};
  border-radius: 24px;
  box-shadow:
    0 4px 20px -4px rgba(0, 0, 0, 0.08),
    0 2px 8px -2px rgba(0, 0, 0, 0.04);

  background: ${({ theme }) => theme.colors.white};

  @media (width <= 768px) {
    padding: 20px;
    border-radius: 20px;
  }
`;

const SectionHeader = styled.div`
  margin-bottom: 20px;

  display: flex;
  gap: 12px;
  align-items: center;
`;

const SectionIconBox = styled.span`
  width: 32px;
  height: 32px;
  border-radius: 16px;

  display: flex;
  align-items: center;
  justify-content: center;

  background: linear-gradient(135deg, ${({ theme }) => theme.colors.primary}, ${({ theme }) => theme.colors.primaryLight});
  box-shadow: 0 2px 8px rgba(255, 153, 102, 0.3);
  
  svg {
    color: white;
  }
`;

const SectionTitle = styled.h2`
  margin: 0;

  color: ${({ theme }) => theme.colors.black};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const TagContainer = styled.div`
  margin-bottom: 20px;

  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  @media (width <= 768px) {
    margin-bottom: 16px;
  }
`;

const TrendyGrid = styled.div<{ deviceType: DeviceType }>`
  display: grid;
  gap: 16px;

  grid-template-columns: ${({ deviceType }) =>
    deviceType === 'mobile' ? '1fr' : 'repeat(2, 1fr)'};

  @media (width <= 1024px) and (width > 768px) {
    gap: 12px;
  }
`;

const NewsletterCard = styled(ImageInfoCard)`
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.dividers};
  border-radius: 16px;
  transition: all 0.2s ease;
  cursor: pointer;
  background: ${({ theme }) => theme.colors.white};

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px -8px rgba(0, 0, 0, 0.12);
    border-color: ${({ theme }) => theme.colors.primary};
  }

  &:active {
    transform: translateY(-2px);
  }

  &:focus-visible {
    outline: 2px solid ${({ theme }) => theme.colors.primary};
    outline-offset: 2px;
  }

  &:focus:not(:focus-visible) {
    outline: none;
  }
`;
