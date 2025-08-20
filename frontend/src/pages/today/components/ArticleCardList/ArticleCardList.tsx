import styled from '@emotion/styled';
import ArticleCard from '../ArticleCard/ArticleCard';
import EmptyLetterCard from '../EmptyLetterCard/EmptyLetterCard';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';
import { theme } from '@/styles/theme';
import { components } from '@/types/openapi';
import CheckIcon from '#/assets/check.svg';
import LetterIcon from '#/assets/letter.svg';

interface ArticleCardListProps {
  articles: components['schemas']['ArticleResponse'][];
}

const ArticleCardList = ({ articles }: ArticleCardListProps) => {
  const deviceType = useDeviceType();
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

  if (articles.length === 0)
    return <EmptyLetterCard title="새로운 뉴스레터가 없어요" />;

  return (
    <Container>
      <ListTitleBox>
        <LetterIcon width={32} height={32} color={theme.colors.white} />
        <ListTitle>새로운 뉴스레터 ({grouped.unread.length}개)</ListTitle>
      </ListTitleBox>
      <CardList deviceType={deviceType}>
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
      <CardList deviceType={deviceType}>
        {grouped.read.map((article) => (
          <li key={article.articleId}>
            <ArticleCard data={article} />
          </li>
        ))}
      </CardList>
    </Container>
  );
};

export default ArticleCardList;

const Container = styled.div`
  width: 100%;

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

const CardList = styled.ul<{ deviceType: DeviceType }>`
  width: 100%;

  display: flex;
  gap: ${({ deviceType }) => (deviceType === 'mobile' ? '8px' : '16px')};
  flex-direction: column;
`;
