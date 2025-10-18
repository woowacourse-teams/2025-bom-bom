import { theme } from '@bombom/shared/theme';
import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDevice } from '@/hooks/useDevice';
import {
  GUIDE_MAIL_STORAGE_KEY,
  GUIDE_MAILS,
} from '@/pages/guide-detail/constants/guideMail';
import QuickMenu from '@/pages/storage/components/QuickMenu/QuickMenu';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import { createStorage } from '@/utils/localStorage';
import type { Device } from '@/hooks/useDevice';
import type { LocalGuideMail } from '@/types/guide';
import HelpIcon from '#/assets/svg/help.svg';

export const Route = createFileRoute('/_bombom/guide')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
      {
        title: '봄봄 | 가이드 보관함',
      },
    ],
  }),
  component: GuidePage,
});

function GuidePage() {
  const guideMailReadMailIds =
    createStorage<LocalGuideMail>(GUIDE_MAIL_STORAGE_KEY).get()?.readMailIds ??
    [];

  const guideArticles = GUIDE_MAILS.map((article) => {
    return {
      ...article,
      isRead: guideMailReadMailIds.includes(article.articleId),
    };
  });

  const device = useDevice();
  if (!guideArticles) return null;

  return (
    <Container>
      <TitleWrapper>
        <GuideIcon fill={theme.colors.white} />
        <Title>가이드 메일 보관함</Title>
      </TitleWrapper>

      <ContentWrapper device={device}>
        <SidebarSection device={device}>
          <QuickMenu />
        </SidebarSection>

        <ArticleList>
          {guideArticles.map((article) => (
            <li key={article.articleId}>
              <ArticleCard
                data={article}
                readVariant="badge"
                to={`/articles/guide/${article.articleId}`}
              />
            </li>
          ))}
        </ArticleList>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 1280px;

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading3};
`;

const GuideIcon = styled(HelpIcon)`
  width: 36px;
  height: 36px;
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  text-align: center;
`;

const ContentWrapper = styled.div<{ device: Device }>`
  width: 100%;

  display: flex;
  gap: ${({ device }) => (device === 'pc' ? '32px' : '20px')};
  flex-direction: ${({ device }) => (device === 'pc' ? 'row' : 'column')};
`;

const SidebarSection = styled.div<{ device: Device }>`
  width: ${({ device }) => (device === 'pc' ? '320px' : '100%')};

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

const ArticleList = styled.ul`
  width: 100%;

  display: flex;
  gap: 16px;
  flex: 1;
  flex-direction: column;
`;
