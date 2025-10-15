import styled from '@emotion/styled';
import NewsletterSubscribeGuide from './NewsletterSubscribeGuide';
import { openExternalLink } from '@/utils/externalLink';
import ArticleHistoryIcon from '#/assets/svg/article-history.svg';

interface DetailTabProps {
  newsletterDescription: string;
  previousNewsletterUrl?: string;
  isMobile: boolean;
}

const DetailTab = ({
  newsletterDescription,
  previousNewsletterUrl,
  isMobile,
}: DetailTabProps) => {
  const openPreviousLetters = () => {
    if (!previousNewsletterUrl) return;
    openExternalLink(previousNewsletterUrl);
  };

  return (
    <Container isMobile={isMobile}>
      <Description isMobile={isMobile}>{newsletterDescription}</Description>

      {previousNewsletterUrl && (
        <DetailLink onClick={openPreviousLetters} isMobile={isMobile}>
          <ArticleHistoryIcon width={16} height={16} />
          지난 소식 보기
        </DetailLink>
      )}

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

const DetailLink = styled.button<{ isMobile: boolean }>`
  display: flex;
  gap: 4px;
  align-items: center;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};

  transition: all 0.2s ease;

  &:hover {
    color: ${({ theme }) => theme.colors.primary};
    cursor: pointer;
  }
`;
