import { CardList, Container, LettersWrapper } from './ArticleCardList';
import ArticleCardSkeleton from '../ArticleCard/ArticleCardSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { ARTICLE_SIZE } from '@/pages/storage/constants/article';

const ArticleCardListSkeleton = () => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Container isMobile={isMobile}>
      <LettersWrapper isMobile={isMobile}>
        <CardList isMobile={isMobile}>
          {Array.from({ length: ARTICLE_SIZE }).map((_, index) => (
            <li key={`skeleton-${index}`}>
              <ArticleCardSkeleton />
            </li>
          ))}
        </CardList>
      </LettersWrapper>
    </Container>
  );
};

export default ArticleCardListSkeleton;
