import styled from '@emotion/styled';
import ArticleCard from '../ArticleCard/ArticleCard';
import { components } from '@/types/openapi';
import checkIcon from '#/assets/check.svg';
import letterIcon from '#/assets/letter.svg';

interface ArticleCardListProps {
  articles: components['schemas']['ArticleResponse'][];
}

function ArticleCardList({ articles }: ArticleCardListProps) {
  const grouped = articles.reduce<{
    read: components['schemas']['ArticleResponse'][];
    unread: components['schemas']['ArticleResponse'][];
  }>(
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
`;
