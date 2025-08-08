import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { queries } from '@/apis/queries';
import Chip from '@/components/Chip/Chip';
import ImageInfoCard from '@/components/ImageInfoCard/ImageInfoCard';
import { CATEGORIES, CategoryType } from '@/constants/category';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { copyToClipboard } from '@/utils/copy';
import TrendingUpIcon from '#/assets/trending-up.svg';

export default function TrendySection() {
  const { data: newsletters } = useQuery(queries.newsletters());

  const { data: userInfo } = useQuery(queries.me());

  const [selectedCategory, setSelectedCategory] =
    useState<CategoryType>('전체');

  if (!newsletters) return null;

  const filteredNewsletters = newsletters.filter(
    (newsletter) =>
      selectedCategory === '전체' || newsletter.category === selectedCategory,
  );

  const handleCardClick = (url: string) => {
    if (userInfo?.email) {
      copyToClipboard(userInfo.email ?? '');
      alert('이메일이 복사되었습니다. 이 이메일로 뉴스레터를 구독해주세요.');
    }

    window.open(url, '_blank', 'noopener,noreferrer');
  };

  return (
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
      <TrendyGrid>
        {filteredNewsletters.map((newsletter, index) => (
          <ImageInfoCard
            key={index}
            imageUrl={newsletter.imageUrl ?? ''}
            title={newsletter.name ?? ''}
            description={newsletter.description ?? ''}
            onClick={() => {
              handleCardClick(newsletter.subscribeUrl ?? '');
              trackEvent({
                category: 'Newsletter',
                action: 'Click Trendy Newsletter Card',
                label: newsletter.name ?? 'Unknown Newsletter',
              });
            }}
            as="button"
          />
        ))}
      </TrendyGrid>
    </Container>
  );
}

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

const TrendyGrid = styled.div`
  display: grid;
  gap: 8px;

  grid-template-columns: repeat(2, 1fr);

  @media (width <= 768px) {
    grid-template-columns: 1fr;
  }
`;
