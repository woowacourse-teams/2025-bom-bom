import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import Spacing from '@/components/Spacing/Spacing';
import { useLocalStorageState } from '@/hooks/useLocalStorageState';
import { useScrollThreshold } from '@/hooks/useScrollThreshold';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import TodayUnreadArticlesSection from '@/pages/detail/components/TodayUnreadArticlesSection/TodayUnreadArticlesSection';
import GuideArticleBody from '@/pages/guide-detail/components/GuideArticleBody';
import {
  GUIDE_MAIL_STORAGE_KEY,
  GUIDE_MAILS,
} from '@/pages/guide-detail/constants/guideMail';
import { useGuideAsReadMutation } from '@/pages/guide-detail/hooks/useGuideAsReadMutation';
import { formatDate } from '@/utils/date';
import type { LocalGuideMail } from '@/types/guide';

export const Route = createFileRoute('/_bombom/articles/guide/$guideId')({
  head: () => ({
    meta: [
      {
        name: 'robots',
        content: 'noindex, nofollow',
      },
      {
        title: '봄봄 | 가이드 메일 상세',
      },
    ],
  }),
  component: GuideMailPage,
});

function GuideMailPage() {
  const { guideId } = Route.useParams();
  const guideIdNumber = Number(guideId);
  const [guideArticles, setGuideArticles] = useLocalStorageState<
    LocalGuideMail,
    string
  >(GUIDE_MAIL_STORAGE_KEY);

  const guideArticle = GUIDE_MAILS.find(
    (article) => article.articleId === guideIdNumber,
  );
  const isRead = guideArticles?.readMailIds?.includes(guideIdNumber) ?? false;

  const updateGuideStateAsRead = () => {
    setGuideArticles((prev) => {
      if (!prev) {
        return {
          createdAt: formatDate(new Date('2025-01-01')),
          readMailIds: [guideIdNumber],
        };
      }

      if (prev.readMailIds.includes(guideIdNumber)) {
        return prev;
      }

      return {
        ...prev,
        readMailIds: [...prev.readMailIds, guideIdNumber],
      };
    });
  };

  const { mutate: updateGuideAsRead } = useGuideAsReadMutation({
    onSuccess: updateGuideStateAsRead,
  });

  useScrollThreshold({
    enabled: !isRead && !!guideArticle,
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

      <GuideArticleBody articleId={guideIdNumber} />

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
