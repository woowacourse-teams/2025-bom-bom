import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import Spacing from '@/components/Spacing/Spacing';
import { useLocalStorageState } from '@/hooks/useLocalStorageState';
import { useScrollThreshold } from '@/hooks/useScrollThreshold';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import TodayUnreadArticlesSection from '@/pages/detail/components/TodayUnreadArticlesSection/TodayUnreadArticlesSection';
import useGuideAsReadMutation from '@/pages/detail/hooks/useGuideAsReadMutation';
import GuideMail1 from '@/pages/guide-detail/components/GuideMail1';
import GuideMail2 from '@/pages/guide-detail/components/GuideMail2';
import GuideMail3 from '@/pages/guide-detail/components/GuideMail3';
import { GUIDE_MAILS } from '@/pages/guide-detail/constants/guideMail';
import { formatDate } from '@/utils/date';

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
    {
      createdAt: string;
      readMailIds: number[];
    },
    string
  >('guide-mail');

  const guideArticle = GUIDE_MAILS.find(
    (article) => article.articleId === guideIdNumber,
  );
  const isRead = guideArticles?.readMailIds?.includes(guideIdNumber) ?? false;

  const updateGuideStateAsRead = () => {
    setGuideArticles((prev) => {
      if (!prev) {
        return {
          createdAt: formatDate(new Date()),
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

      {guideIdNumber === 1 ? (
        <GuideMail1 />
      ) : guideIdNumber === 2 ? (
        <GuideMail2 />
      ) : (
        <GuideMail3 />
      )}

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
