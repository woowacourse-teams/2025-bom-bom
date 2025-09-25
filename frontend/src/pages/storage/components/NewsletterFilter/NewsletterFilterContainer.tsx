import styled from '@emotion/styled';
import Tabs from '@/components/Tabs/Tabs';
import { useDevice } from '@/hooks/useDevice';
import { theme } from '@/styles/theme';
import type { ComponentProps, ReactElement } from 'react';
import NewsIcon from '#/assets/svg/news.svg';

interface NewsletterFilterContainerProps {
  children:
    | ReactElement<ComponentProps<'li'>>[]
    | ReactElement<ComponentProps<'li'>>;
}

const NewsletterFilterContainer = ({
  children,
}: NewsletterFilterContainerProps) => {
  const device = useDevice();
  const isPc = device === 'pc';

  return (
    <Container aria-label="뉴스레터" isPc={isPc}>
      {isPc && (
        <TitleWrapper>
          <IconWrapper>
            <NewsIcon width={16} height={16} fill={theme.colors.white} />
          </IconWrapper>
          <Title>뉴스레터</Title>
        </TitleWrapper>
      )}
      <StyledTabs direction={isPc ? 'vertical' : 'horizontal'}>
        {children}
      </StyledTabs>
    </Container>
  );
};

export default NewsletterFilterContainer;

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
