import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import Spacing from '@/components/Spacing/Spacing';
import { useLocalStorageState } from '@/hooks/useLocalStorageState';
import { useScrollThreshold } from '@/hooks/useScrollThreshold';
import ArticleBody from '@/pages/detail/components/ArticleBody/ArticleBody';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import TodayUnreadArticlesSection from '@/pages/detail/components/TodayUnreadArticlesSection/TodayUnreadArticlesSection';
import useGuideAsReadMutation from '@/pages/detail/hooks/useGuideAsReadMutation';
import type { GuideMail } from '@/mocks/datas/guideMail';

export const Route = createFileRoute('/_bombom/articles/guide/$guideId')({
  component: GuideMailPage,
});

function GuideMailPage() {
  const { guideId } = Route.useParams();
  const guideIdNumber = Number(guideId);
  const [guideArticles, setGuideArticles] = useLocalStorageState<
    GuideMail[],
    string
  >('guide-mail');
  const guideArticle = guideArticles.find(
    (article) => article.articleId === guideIdNumber,
  );
  const updateGuideStateAsRead = () => {
    setGuideArticles((prev) =>
      prev.map((article) =>
        article.articleId === guideIdNumber
          ? { ...article, isRead: true }
          : article,
      ),
    );
  };
  const { mutate: updateGuideAsRead } = useGuideAsReadMutation({
    onSuccess: updateGuideStateAsRead,
  });

  useScrollThreshold({
    enabled: !guideArticle?.isRead && !!guideArticle,
    threshold: 70,
    throttleMs: 500,
    onTrigger: updateGuideAsRead,
  });

  if (!guideArticle) return null;

  return (
    <Container>
      <ArticleHeader
        title={guideArticle.title}
        newsletterCategory={guideArticle.newsletter.category}
        newsletterName={guideArticle.newsletter.name}
        arrivedDateTime={new Date(guideArticle.arrivedDateTime)}
        expectedReadTime={guideArticle.expectedReadTime}
      />
      <Divider />

      <ArticleBody
        articleId={guideIdNumber}
        articleContent={guideArticle.contents}
      />
      <Spacing size={24} />
      <Divider />

      <ContentDescription>
        이 뉴스레터가 유용했다면 동료들과 공유해주세요. 피드백이나 제안사항이
        있으시면 언제든 연락 주시기 바랍니다.
      </ContentDescription>

      <TodayUnreadArticlesSection articleId={guideIdNumber} />
    </Container>
  );
}

const Container = styled.div`
  max-width: 700px;
  margin: 0 auto;
  margin-top: 20px;
  padding: 28px;
  border-right: 1px solid ${({ theme }) => theme.colors.stroke};
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: center;
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const ContentDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;
