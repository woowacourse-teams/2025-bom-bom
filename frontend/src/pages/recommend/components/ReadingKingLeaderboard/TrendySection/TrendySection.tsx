import styled from '@emotion/styled';
import trendingUpIcon from '../../../../../../public/assets/trending-up.svg';
import Chip from '../../../../../components/Chip/Chip';
import ImageInfoCard from '../../../../../components/ImageInfoCard/ImageInfoCard';
import { CATEGORIES } from '../../../../../constants/category';
import { NewslettersResponse } from '../../../../today/types/article';

interface TrendySectionProps {
  newsletters: NewslettersResponse;
}

export default function TrendySection({ newsletters }: TrendySectionProps) {
  return (
    <Container>
      <SectionHeader>
        <SectionIconBox>
          <img src={trendingUpIcon} alt="trending up" />
        </SectionIconBox>
        <SectionTitle>트렌디한 뉴스레터</SectionTitle>
      </SectionHeader>
      <TagContainer>
        {CATEGORIES.map((category, index) => (
          <Chip key={index} text={category} selected={index === 0} />
        ))}
      </TagContainer>
      <TrendyGrid>
        {newsletters.map((newsletter, index) => (
          <ImageInfoCard
            key={index}
            imageUrl={newsletter.imageUrl}
            name={newsletter.name}
            description={newsletter.description}
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
  display: flex;
  align-items: center;

  margin-bottom: 16px;

  gap: 8px;
`;

const SectionIconBox = styled.span`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 28px;
  height: 28px;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.primary};

  font: ${({ theme }) => theme.fonts.body1};
`;

const SectionTitle = styled.h2`
  margin: 0;

  color: ${({ theme }) => theme.colors.black};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const TagContainer = styled.div`
  display: flex;
  flex-wrap: wrap;

  margin-bottom: 16px;

  gap: 8px;
`;

const TrendyGrid = styled.div`
  display: grid;

  gap: 8px;
  grid-template-columns: repeat(2, 1fr);

  @media (width <= 768px) {
    grid-template-columns: 1fr;
  }
`;
