import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useState } from 'react';
import StorageIcon from '../components/icons/StorageIcon';
import PageLayout from '../components/PageLayout/PageLayout';
import SearchInput from '../components/SearchInput/SearchInput';
import Select from '../components/Select/Select';
import CategoryFilter from '../pages/storage/components/CategoryFilter/CategoryFilter';
import { getArticles, getStatisticsCategories } from '@/apis/articles';
import { getArticleReadStats } from '@/pages/storage/utils/getArticleReadStats';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';

export const Route = createFileRoute('/storage')({
  component: Storage,
});

function Storage() {
  const [selectedCategory, setSelectedCategory] = useState('1');
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const { data: articles } = useQuery({
    queryKey: ['articles', sortFilter, selectedCategory],
    queryFn: () =>
      getArticles({
        memberId: 1,
        sorted: sortFilter,
        category: selectedCategory === '전체' ? undefined : selectedCategory,
      }),
  });
  const { data: categoryCounts } = useQuery({
    queryKey: ['/articles/statistics/categories'],
    queryFn: () => getStatisticsCategories({ memberId: 1 }),
  });

  if (!articles) return null;

  const readStats = getArticleReadStats(articles.content);

  return (
    <PageLayout activeNav="storage">
      <Container>
        <SideSection>
          <CategoryFilter
            categoryList={[
              {
                value: '0',
                label: '전체',
                quantity: categoryCounts?.totalCount ?? 0,
              },
              ...(categoryCounts?.categories.map(
                ({ category, count }, index) => ({
                  value: String(index + 1),
                  label: category,
                  quantity: count,
                }),
              ) ?? []),
            ]}
            selectedValue={selectedCategory}
            onSelectCategory={(value) => setSelectedCategory(value)}
          />
        </SideSection>
        <MainSection>
          <TitleWrapper>
            <TitleIconBox>
              <StorageIcon color="white" />
            </TitleIconBox>
            <Title>뉴스레터 보관함</Title>
          </TitleWrapper>
          <SearchInput placeholder="뉴스레터 제목이나 발행처로 검색하세요..." />
          <SummaryBar>
            <SummaryText>
              총 {readStats.total}개 • 읽지 않음 {readStats.unread}개 • 읽음{' '}
              {readStats.read}개
            </SummaryText>
            <Select
              options={[
                { value: 'DESC', label: '최신순' },
                { value: 'ASC', label: '오래된순' },
              ]}
              selectedValue={sortFilter}
              onSelectOption={(value) => setSortFilter(value)}
            />
          </SummaryBar>
          <ArticleList>
            {articles.content.map((article) => (
              <li key={article.articleId}>
                <ArticleCard data={article} />
              </li>
            ))}
          </ArticleList>
        </MainSection>
      </Container>
    </PageLayout>
  );
}

const Container = styled.div`
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: center;

  width: 100%;
  max-width: 1280px;
  padding: 64px 0;
`;

const SideSection = styled.div`
  margin-top: 70px;
`;

const MainSection = styled.div`
  display: flex;
  gap: 20px;
  flex-direction: column;
  align-items: flex-start;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const TitleIconBox = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 28px;
  height: 28px;
  padding: 6px;
  border-radius: 14px;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading2};
`;

const SummaryBar = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;

  width: 100%;
`;

const SummaryText = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ArticleList = styled.ul`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;
