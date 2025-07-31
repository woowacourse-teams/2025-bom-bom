import styled from '@emotion/styled';
import ArticleCard from '../ArticleCard/ArticleCard';
import EmptyLetterCard from '../EmptyLetterCard/EmptyLetterCard';
import { Article } from '@/pages/today/types/article';
import { theme } from '@/styles/theme';
import CheckIcon from '#/assets/check.svg';
import LetterIcon from '#/assets/letter.svg';

interface ArticleCardListProps {
  articles: Article[];
}

function ArticleCardList({ articles }: ArticleCardListProps) {
  const grouped = articles.reduce<{ read: Article[]; unread: Article[] }>(
    (acc, article) => {
      if (article.isRead) acc.read.push(article);
      else acc.unread.push(article);
      return acc;
    },
    { read: [], unread: [] },
  );

  if (articles.length === 0)
    return <EmptyLetterCard title="새로운 뉴스레터가 없어요" />;

  return (
    <Container>
      <ListTitleBox>
        <LetterIcon width={32} height={32} color={theme.colors.white} />
        <ListTitle>새로운 뉴스레터 ({grouped.unread.length}개)</ListTitle>
      </ListTitleBox>
      <CardList>
        {grouped.unread.map((article) => (
          <li key={article.articleId}>
            <ArticleCard data={article} />
          </li>
        ))}
      </CardList>
      <ListTitleBox>
        <CheckIcon width={32} height={32} color={theme.colors.black} />
        <ListTitle>읽은 뉴스레터 ({grouped.read.length}개)</ListTitle>
      </ListTitleBox>
      <CardList>
        {grouped.read.map((article) => (
          <li key={article.articleId}>
            <ArticleCard data={article} />
          </li>
        ))}
      </CardList>
    </Container>
  );
}

export default ArticleCardList;

const Container = styled.div`
  display: flex;
  gap: 16px;
  flex-direction: column;
  align-items: flex-start;

  width: 100%;
`;

const ListTitleBox = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
`;

const ListTitle = styled.h5`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const CardList = styled.ul`
  display: flex;
  gap: 16px;
  flex-direction: column;

  width: 100%;
`;
