import styled from '@emotion/styled';
import NewsletterSubscribeGuide from './NewsletterSubscribeGuide';

interface DetailTabProps {
  newsletterDescription: string;
  isMobile: boolean;
}

const DetailTab = ({ newsletterDescription, isMobile }: DetailTabProps) => {
  return (
    <Container isMobile={isMobile}>
      <Description isMobile={isMobile}>{newsletterDescription}</Description>

      {!isMobile && <NewsletterSubscribeGuide />}
    </Container>
  );
};

export default DetailTab;

const Container = styled.div<{ isMobile: boolean }>`
  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '16px' : '24px')};
  flex-direction: column;
`;

const Description = styled.p<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ isMobile, theme }) =>
    isMobile ? theme.fonts.body2 : theme.fonts.body1};
`;
