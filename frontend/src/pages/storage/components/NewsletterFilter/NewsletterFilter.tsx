import styled from '@emotion/styled';
import Badge from '@/components/Badge/Badge';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';
import { useDeviceType } from '@/hooks/useDeviceType';
import { theme } from '@/styles/theme';
import { Newsletter } from '@/types/articles';
import NewsIcon from '#/assets/news.svg';

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
  const deviceType = useDeviceType();

  return (
    <Container aria-label="뉴스레터" isPc={deviceType === 'pc'}>
      {deviceType === 'pc' && (
        <TitleWrapper>
          <IconWrapper>
            <NewsIcon width={16} height={16} fill={theme.colors.white} />
          </IconWrapper>
          <Title>뉴스레터</Title>
        </TitleWrapper>
      )}
      <StyledTabs direction={deviceType === 'pc' ? 'vertical' : 'horizontal'}>
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
            textAlign={deviceType === 'pc' ? 'start' : 'center'}
          />
        ))}
      </StyledTabs>
    </Container>
  );
}

export default NewsLetterFilter;

const Container = styled.nav<{ isPc: boolean }>`
  width: 100%;
  padding: ${({ isPc }) => (isPc ? '16px' : '0')};
  border: ${({ isPc, theme }) =>
    isPc ? `1px solid ${theme.colors.stroke}` : 'none'};
  border-radius: 20px;

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
`;

const IconWrapper = styled.div`
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading5};
`;

const NewsLetterImage = styled.img`
  width: 24px;
  height: 24px;
  border-radius: 8px;

  object-fit: cover;
`;

const StyledTabs = styled(Tabs)`
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
