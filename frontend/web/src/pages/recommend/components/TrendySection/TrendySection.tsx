import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { parseAsInteger, useQueryState } from 'nuqs';
import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import NewsletterList from './NewsletterList';
import NewsletterDetail from '../NewsletterDetail/NewsletterDetail';
import { queries } from '@/apis/queries';
import Chip from '@/components/Chip/Chip';
import ImageInfoCardSkeleton from '@/components/ImageInfoCard/ImageInfoCardSkeleton';
import Modal from '@/components/Modal/Modal';
import useModal from '@/components/Modal/useModal';
import SearchInput from '@/components/SearchInput/SearchInput';
import { CATEGORIES, NEWSLETTER_COUNT } from '@/constants/newsletter';
import { useDevice } from '@/hooks/useDevice';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import type { Category } from '@/constants/newsletter';
import type { Device } from '@/hooks/useDevice';
import type { Newsletter } from '@/types/newsletter';
import CloseIcon from '#/assets/svg/close.svg';
import ReadingGlassesIcon from '#/assets/svg/reading-glasses.svg';
import TrendingUpIcon from '#/assets/svg/trending-up.svg';

const TrendySection = () => {
  const device = useDevice();
  const [selectedCategory, setSelectedCategory] = useState<Category>('전체');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isSearchExpanded, setIsSearchExpanded] = useState<boolean>(false);
  const searchInputRef = useRef<HTMLInputElement>(null);
  const searchContainerRef = useRef<HTMLDivElement>(null);

  const [selectedNewsletterId, setSelectedNewsletterId] = useQueryState(
    'newsletterDetail',
    parseAsInteger,
  );
  const { data: newsletters, isLoading } = useQuery(queries.newsletters());

  const {
    modalRef: detailModalRef,
    openModal: openDetailModal,
    closeModal: closeDetailModal,
    isOpen,
  } = useModal({
    onClose: () => {
      setSelectedNewsletterId(null);
    },
  });

  const filteredNewsletters = newsletters?.filter((newsletter) => {
    const matchesCategory =
      selectedCategory === '전체' || newsletter.category === selectedCategory;
    const matchesSearch =
      searchQuery === '' ||
      newsletter.name.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const handleCardClick = (newsletter: Newsletter) => {
    setSelectedNewsletterId(newsletter.newsletterId);
    trackEvent({
      category: 'Newsletter',
      action: '뉴스레터 카드 클릭',
      label: newsletter.name ?? 'Unknown Newsletter',
    });
  };

  useEffect(() => {
    if (selectedNewsletterId) {
      const newsletter = newsletters?.find(
        (newsletter) => newsletter.newsletterId === selectedNewsletterId,
      );
      if (newsletter) {
        openDetailModal();
      }
    } else {
      closeDetailModal();
    }
  }, [closeDetailModal, selectedNewsletterId, newsletters, openDetailModal]);

  useEffect(() => {
    if (isSearchExpanded && searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, [isSearchExpanded]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        searchContainerRef.current &&
        !searchContainerRef.current.contains(event.target as Node)
      ) {
        setIsSearchExpanded(false);
      }
    };

    if (isSearchExpanded) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isSearchExpanded]);

  return (
    <>
      <Container>
        <SectionHeader>
          <HeaderLeft>
            <SectionIconBox>
              <TrendingUpIcon width={16} height={16} />
            </SectionIconBox>
            <SectionTitle>트렌디한 뉴스레터</SectionTitle>
          </HeaderLeft>
          <SearchIconButton
            onClick={() => setIsSearchExpanded(true)}
            aria-label="검색 열기"
          >
            <ReadingGlassesIcon width={20} height={20} />
          </SearchIconButton>
          <ExpandableSearchContainer
            ref={searchContainerRef}
            isExpanded={isSearchExpanded}
          >
            <SearchInput
              ref={searchInputRef}
              placeholder="뉴스레터 이름으로 검색"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              aria-label="뉴스레터 검색"
            />
            <CloseButton
              onClick={() => setIsSearchExpanded(false)}
              aria-label="검색 닫기"
            >
              <CloseIcon width={20} height={20} />
            </CloseButton>
          </ExpandableSearchContainer>
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
        <TrendyGrid
          device={device}
          hasContent={!!(filteredNewsletters && filteredNewsletters.length > 0)}
        >
          {isLoading ? (
            Array.from({
              length:
                device === 'mobile'
                  ? NEWSLETTER_COUNT.mobile
                  : NEWSLETTER_COUNT.nonMobile,
            }).map((_, index) => (
              <ImageInfoCardSkeleton key={`skeleton-card-${index}`} />
            ))
          ) : (
            <NewsletterList
              key={selectedCategory}
              newsletters={filteredNewsletters ?? []}
              handleCardClick={handleCardClick}
            />
          )}
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
          {selectedNewsletterId && (
            <NewsletterDetail newsletterId={selectedNewsletterId} />
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
  position: relative;
  margin-bottom: 16px;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
`;

const HeaderLeft = styled.div`
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

const SearchIconButton = styled.button`
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

  background: transparent;
  color: ${({ theme }) => theme.colors.textSecondary};

  cursor: pointer;
  transition: all 0.2s ease-in-out;

  &:hover {
    background: ${({ theme }) => theme.colors.dividers};
    color: ${({ theme }) => theme.colors.textPrimary};
  }

  &:active {
    transform: scale(0.95);
  }
`;

const ExpandableSearchContainer = styled.div<{ isExpanded: boolean }>`
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  z-index: 10;

  display: flex;
  gap: 8px;
  align-items: center;

  background: ${({ theme }) => theme.colors.white};

  opacity: ${({ isExpanded }) => (isExpanded ? 1 : 0)};
  pointer-events: ${({ isExpanded }) => (isExpanded ? 'auto' : 'none')};

  transform: translateX(${({ isExpanded }) => (isExpanded ? '0' : '20px')});

  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
`;

const CloseButton = styled.button`
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 8px;

  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.dividers};
  color: ${({ theme }) => theme.colors.textPrimary};

  cursor: pointer;
  transition: all 0.2s ease-in-out;

  &:hover {
    background: ${({ theme }) => theme.colors.stroke};
  }

  &:active {
    transform: scale(0.95);
  }
`;

const TagContainer = styled.div`
  margin-bottom: 16px;

  display: flex;
  gap: 8px;
  flex-wrap: wrap;
`;

const TrendyGrid = styled.div<{ device: Device; hasContent: boolean }>`
  height: ${({ device }) => (device === 'mobile' ? '400px' : '600px')};

  display: grid;
  gap: 12px;

  grid-auto-rows: min-content;

  grid-template-columns: ${({ device, hasContent }) =>
    device === 'mobile' || !hasContent ? '1fr' : 'repeat(2, 1fr)'};
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    border-radius: 4px;
    background: ${({ theme }) => theme.colors.dividers};
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 4px;
    background: ${({ theme }) => theme.colors.stroke};

    &:hover {
      background: ${({ theme }) => theme.colors.textTertiary};
    }
  }
`;
