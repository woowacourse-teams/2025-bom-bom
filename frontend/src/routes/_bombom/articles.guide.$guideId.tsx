import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useState } from 'react';
import Spacing from '@/components/Spacing/Spacing';
import { GUIDE_MAILS } from '@/mocks/datas/guideMail';
import ArticleBody from '@/pages/detail/components/ArticleBody/ArticleBody';
import ArticleHeader from '@/pages/detail/components/ArticleHeader/ArticleHeader';
import FloatingActionButtons from '@/pages/detail/components/FloatingActionButtons/FloatingActionButtons';
import TodayUnreadArticlesSection from '@/pages/detail/components/TodayUnreadArticlesSection/TodayUnreadArticlesSection';

export const Route = createFileRoute('/_bombom/articles/guide/$guideId')({
  component: GuideMailPage,
});

function GuideMailPage() {
  const { guideId } = Route.useParams();
  const guideIdNumber = Number(guideId);

  const [isBookmarked, setIsBookmarked] = useState(false);

  const toggleBookmark = () => {
    setIsBookmarked((prev) => !prev);
  };

  const firstGuideMail = GUIDE_MAILS[0]!;

  return (
    <Container>
      <ArticleHeader
        title={firstGuideMail.title}
        newsletterCategory={firstGuideMail.newsletter.category}
        newsletterName={firstGuideMail.newsletter.name}
        arrivedDateTime={new Date(firstGuideMail.arrivedDateTime)}
        expectedReadTime={firstGuideMail.expectedReadTime}
      />
      <Divider />

      <ArticleBody
        articleId={guideIdNumber}
        articleContent={firstGuideMail.contents}
      />
      <Spacing size={24} />
      <Divider />

      <ContentDescription>
        이 뉴스레터가 유용했다면 동료들과 공유해주세요. 피드백이나 제안사항이
        있으시면 언제든 연락 주시기 바랍니다.
      </ContentDescription>

      <TodayUnreadArticlesSection articleId={guideIdNumber} />

      <FloatingActionButtons
        bookmarked={isBookmarked}
        onBookmarkClick={toggleBookmark}
      />
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
