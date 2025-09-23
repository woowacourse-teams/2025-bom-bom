import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDevice } from '@/hooks/useDevice';
import QuickMenu from '@/pages/storage/components/QuickMenu/QuickMenu';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import { theme } from '@/styles/theme';
import { createStorage } from '@/utils/localStorage';
import type { Device } from '@/hooks/useDevice';
import type { Article } from '@/types/articles';
import HelpIcon from '#/assets/help.svg';

export const Route = createFileRoute('/_bombom/guide')({
  component: GuidePage,
});

function GuidePage() {
  const guideArticles = createStorage<Article[], string>('guide-mail').get();

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
