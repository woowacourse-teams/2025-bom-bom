import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import clockIcon from '../../public/assets/clock.svg';
import Chip from '../components/Chip/Chip';
import NewsletterItemCard from '../pages/detail/components/NewsletterItemCard/NewsletterItemCard';
import { formatDateToDotString } from '../utils/date';
import { getArticleById, getArticles } from '@/apis/articles';

export const Route = createFileRoute('/articles/$articleId')({
  component: ArticleDetailPage,
});

function ArticleDetailPage() {
  const { articleId } = Route.useParams();

  const { data: currentArticle } = useQuery({
    queryKey: ['article', articleId],
    queryFn: () =>
      getArticleById({
        articleId: Number(articleId),
        memberId: 1,
      }),
  });

  const { data: otherArticles } = useQuery({
    queryKey: ['otherArticles'],
    queryFn: () =>
      getArticles({ date: new Date(), memberId: 1, sorted: 'ASC' }),
  });

  if (!currentArticle || !otherArticles) return null;

  return (
    <Container>
      <HeaderWrapper>
        <Title>{currentArticle.title}</Title>
        <MetaInfoRow>
          <Chip text="기술" />
          <MetaInfoText>from {currentArticle.newsletter.name}</MetaInfoText>
          <MetaInfoText>
            {formatDateToDotString(new Date(currentArticle.arrivedDateTime))}
          </MetaInfoText>
          <ReadTimeBox>
            <img src={clockIcon} alt="시계 아이콘" />
            <MetaInfoText>{currentArticle.expectedReadTime}분</MetaInfoText>
          </ReadTimeBox>
        </MetaInfoRow>
      </HeaderWrapper>
      <Divider />
      <ContentWrapper
        dangerouslySetInnerHTML={{ __html: currentArticle.contents ?? '' }}
      />
      <Divider />
      <ContentDescription>
        이 뉴스레터가 유용했다면 동료들과 공유해주세요. 피드백이나 제안사항이
        있으시면 언제든 연락 주시기 바랍니다.
      </ContentDescription>
      <TodayArticlesWrapper>
        <TodayArticleTitle>오늘 읽지 않은 다른 아티클</TodayArticleTitle>
        <TodayArticleList>
          {otherArticles.content.map((article) => (
            <NewsletterItemCard key={article.articleId} data={article} />
          ))}
        </TodayArticleList>
      </TodayArticlesWrapper>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  max-width: 700px;
  margin: 0 auto;
  margin-top: 20px;
  padding: 28px;
  border-right: 1px solid ${({ theme }) => theme.colors.stroke};
  border-left: 1px solid ${({ theme }) => theme.colors.stroke};

  gap: 20px;
`;

const HeaderWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;

  gap: 12px;
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading1};
`;

const MetaInfoRow = styled.div`
  display: flex;
  align-items: center;

  gap: 8px;
`;

const MetaInfoText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  align-items: center;

  gap: 4px;
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const ContentDescription = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const TodayArticlesWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  gap: 12px;
`;

const TodayArticleTitle = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading3};
`;

const TodayArticleList = styled.div`
  display: grid;

  gap: 20px;

  grid-template-columns: repeat(2, 1fr);
`;
