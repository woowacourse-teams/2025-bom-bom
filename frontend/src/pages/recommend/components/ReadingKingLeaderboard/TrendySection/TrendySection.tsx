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
      <Container deviceType={deviceType}>
        <SectionHeader>
          <SectionIconBox>
            <TrendingUpIcon width={16} height={16} />
          </SectionIconBox>
          <SectionTitle>트렌디한 뉴스레터</SectionTitle>
        </SectionHeader>
        <TagContainer deviceType={deviceType}>
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

const Container = styled.div<{ deviceType: DeviceType }>`
  width: 100%;
  padding: ${({ deviceType }) => (deviceType === 'mobile' ? '20px' : '28px')};
  border: 1px solid ${({ theme }) => theme.colors.dividers};
  border-radius: ${({ deviceType }) =>
    deviceType === 'mobile' ? '20px' : '24px'};
  box-shadow:
    0 4px 20px -4px rgb(0 0 0 / 8%),
    0 2px 8px -2px rgb(0 0 0 / 4%);

  background: ${({ theme }) => theme.colors.white};
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
  box-shadow: 0 2px 8px rgb(255 153 102 / 30%);

  display: flex;
  align-items: center;
  justify-content: center;

  background: linear-gradient(
    135deg,
    ${({ theme }) => theme.colors.primary},
    ${({ theme }) => theme.colors.primaryLight}
  );

  svg {
    color: white;
  }
`;

const SectionTitle = styled.h2`
  margin: 0;

  color: ${({ theme }) => theme.colors.black};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const TagContainer = styled.div<{ deviceType: DeviceType }>`
  margin-bottom: ${({ deviceType }) =>
    deviceType === 'mobile' ? '16px' : '20px'};

  display: flex;
  gap: 8px;
  flex-wrap: wrap;
`;

const TrendyGrid = styled.div<{ deviceType: DeviceType }>`
  display: grid;
  gap: ${({ deviceType }) => (deviceType === 'tablet' ? '12px' : '16px')};

  grid-template-columns: ${({ deviceType }) =>
    deviceType === 'pc' ? 'repeat(2, 1fr)' : '1fr'};
`;

const NewsletterCard = styled(ImageInfoCard)`
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.dividers};
  border-radius: 16px;

  background: ${({ theme }) => theme.colors.white};

  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 8px 25px -8px rgb(0 0 0 / 12%);

    border-color: ${({ theme }) => theme.colors.primary};
    transform: translateY(-4px);
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
