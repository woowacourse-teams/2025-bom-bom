import styled from '@emotion/styled';
import ArticleCard from './ArticleCard';
import checkIcon from '../../../../public/assets/check.svg';
import letterIcon from '../../../../public/assets/letter.svg';
import { Article } from '../types/article';

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

  return (
    <Container>
      <ListTitleBox>
        <img src={letterIcon} alt="새로운 뉴스레터 타이틀 이미지" />
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
        <img src={checkIcon} alt="새로운 뉴스레터 타이틀 이미지" />
        <ListTitle>읽은 뉴스레터 ({grouped.read.length}개)</ListTitle>
      </ListTitleBox>
      <CardList>
        {grouped.read.map((article) => (
          <li key={article.articleId}>
            <ArticleCard key={article.articleId} data={article} />
          </li>
        ))}
      </CardList>
    </Container>
  );
}

export default ArticleCardList;

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  gap: 16px;
`;

const ListTitleBox = styled.div`
  display: flex;
  align-items: center;

  gap: 12px;
`;

const ListTitle = styled.h5`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const CardList = styled.ul`
  display: flex;
  flex-direction: column;

  gap: 16px;
`;
