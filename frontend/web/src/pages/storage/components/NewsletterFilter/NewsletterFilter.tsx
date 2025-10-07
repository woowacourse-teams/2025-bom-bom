import styled from '@emotion/styled';
import { theme } from 'shared/theme';
import Badge from '@/components/Badge/Badge';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';
import { useDevice } from '@/hooks/useDevice';
import type { Newsletter } from '@/types/articles';
import NewsIcon from '#/assets/svg/news.svg';

interface NewsLetterFilterProps {
  newsLetterList: Newsletter['newsletters'];
  selectedNewsletterId: number | null;
  onSelectNewsletter: (id: number | null) => void;
}

function NewsLetterFilter({
  newsLetterList,
  selectedNewsletterId,
  onSelectNewsletter,
}: NewsLetterFilterProps) {
  const device = useDevice();

  return (
    <Container aria-label="뉴스레터" isPc={device === 'pc'}>
      {device === 'pc' && (
        <TitleWrapper>
          <IconWrapper>
            <NewsIcon width={16} height={16} fill={theme.colors.white} />
          </IconWrapper>
          <Title>뉴스레터</Title>
        </TitleWrapper>
      )}
      <StyledTabs direction={device === 'pc' ? 'vertical' : 'horizontal'}>
        {newsLetterList.map(({ name, articleCount, imageUrl, id }) => (
          <Tab
            key={name}
            value={id ?? null}
            label={name}
            selected={
              selectedNewsletterId === null
                ? name === '전체'
                : id === selectedNewsletterId
            }
            onTabSelect={onSelectNewsletter}
            StartComponent={
              imageUrl ? <NewsLetterImage src={imageUrl} /> : null
            }
            EndComponent={<Badge text={String(articleCount)} />}
            textAlign={device === 'pc' ? 'start' : 'center'}
          />
        ))}
      </StyledTabs>
    </Container>
  );
}

export default NewsLetterFilter;

export const Container = styled.nav<{ isPc: boolean }>`
  width: 100%;
  padding: ${({ isPc }) => (isPc ? '16px' : '0')};
  border: ${({ isPc, theme }) =>
    isPc ? `1px solid ${theme.colors.stroke}` : 'none'};
  border-radius: 20px;

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

export const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
`;

export const IconWrapper = styled.div`
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

export const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading5};
`;

export const NewsLetterImage = styled.img`
  width: 24px;
  height: 24px;
  border-radius: 50%;

  flex-shrink: 0;

  object-fit: cover;
`;

export const StyledTabs = styled(Tabs)`
  padding-bottom: 8px;
  overflow-x: auto;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 2px;
    background: ${({ theme }) => theme.colors.stroke};
  }
`;
