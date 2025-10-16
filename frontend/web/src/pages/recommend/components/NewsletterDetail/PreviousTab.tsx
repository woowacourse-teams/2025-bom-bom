import { theme } from '@bombom/shared';
import styled from '@emotion/styled';
import { useNavigate } from '@tanstack/react-router';
import PreviousArticleListItem from './PreviousArticleListItem';
import { openExternalLink } from '@/utils/externalLink';
import type { GetPreviousArticlesResponse } from '@/apis/previousArticles';
import sadBom from '#/assets/avif/sad-bom.avif';
import subscribeBom from '#/assets/avif/subscribe-bom.avif';
import OpenIcon from '#/assets/svg/open.svg';

interface PreviousTabProps {
  newsletterSubscribeUrl: string;
  previousArticles?: GetPreviousArticlesResponse | null;
  previousNewsletterUrl?: string;
  isMobile: boolean;
}

const PreviousTab = ({
  newsletterSubscribeUrl,
  previousArticles,
  previousNewsletterUrl,
  isMobile,
}: PreviousTabProps) => {
  const navigate = useNavigate();

  const openPreviousLetters = () => {
    if (!previousNewsletterUrl) return;
    openExternalLink(previousNewsletterUrl);
  };

  if (previousArticles?.length) {
    return previousArticles.map((article) => (
      <PreviousArticleListItem
        key={article.articleId}
        title={article.title}
        contentsSummary={article.contentsSummary}
        expectedReadTime={article.expectedReadTime}
        onClick={() =>
          navigate({
            to: `articles/previous/${article.articleId}`,
            state: {
              subscribeUrl: newsletterSubscribeUrl,
            },
          })
        }
      />
    ));
  }

  if (previousNewsletterUrl) {
    return (
      <Container>
        <img width={isMobile ? 120 : 160} src={subscribeBom} alt="empty" />
        <OpenSubscribeButton onClick={openPreviousLetters}>
          지난 소식 보러가기
          <OpenIcon fill={theme.colors.primary} width={16} height={16} />
        </OpenSubscribeButton>
      </Container>
    );
  }

  return (
    <Container>
      <img width={isMobile ? 160 : 200} src={sadBom} alt="empty" />
      지난 뉴스레터가 존재하지 않습니다.
    </Container>
  );
};

export default PreviousTab;

const Container = styled.div`
  padding: 24px 0;

  display: flex;
  gap: 8px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const OpenSubscribeButton = styled.button`
  width: fit-content;
  padding: 8px 16px;
  border: 1px solid ${({ theme }) => theme.colors.primary};
  border-radius: 16px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};
`;
