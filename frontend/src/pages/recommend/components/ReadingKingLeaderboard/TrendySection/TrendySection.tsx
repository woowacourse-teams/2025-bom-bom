import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import NewsletterCardWithModal from './NewsletterCardWithModal';
import { queries } from '@/apis/queries';
import Chip from '@/components/Chip/Chip';
import { CATEGORIES, CategoryType } from '@/constants/category';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import TrendingUpIcon from '#/assets/trending-up.svg';

const TrendySection = () => {
  const [selectedCategory, setSelectedCategory] =
    useState<CategoryType>('전체');

  const { data: newsletters } = useQuery(queries.newsletters());

  const deviceType = useDeviceType();

  if (!newsletters) return null;

  const filteredNewsletters = newsletters.filter(
    (newsletter) =>
      selectedCategory === '전체' || newsletter.category === selectedCategory,
  );

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
      <TrendyGrid deviceType={deviceType}>
        {filteredNewsletters.map((newsletter) => (
          <NewsletterCardWithModal
            key={newsletter.newsletterId}
            newsletter={newsletter}
          />
        ))}
      </TrendyGrid>
    </Container>
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

const TrendyGrid = styled.div<{ deviceType: DeviceType }>`
  display: grid;
  gap: 8px;

  grid-template-columns: ${({ deviceType }) =>
    deviceType === 'mobile' ? '1fr' : 'repeat(2, 1fr)'};
`;
