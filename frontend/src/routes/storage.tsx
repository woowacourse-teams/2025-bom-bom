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
import { CategoryType } from '@/constants/category';
import { useDebounce } from '@/hooks/useDebounce';
import { getArticleReadStats } from '@/pages/storage/utils/getArticleReadStats';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';

export const Route = createFileRoute('/storage')({
  component: Storage,
});

function Storage() {
  const [selectedCategory, setSelectedCategory] =
    useState<CategoryType>('전체');
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const [searchInput, setSearchInput] = useState('');
  const { data: articles, refetch: refetchArticles } = useQuery({
    queryKey: ['articles', sortFilter, selectedCategory],
    queryFn: () =>
      getArticles({
        memberId: 1,
        sorted: sortFilter,
        category: selectedCategory === '전체' ? undefined : selectedCategory,
        keyword: searchInput,
      }),
  });
  const { data: categoryCounts } = useQuery({
    queryKey: ['/articles/statistics/categories'],
    queryFn: () => getStatisticsCategories({ memberId: 1 }),
  });

  const debouncedSearch = useDebounce(() => {
    refetchArticles();
  }, 300);

  if (!articles || !categoryCounts) return null;

  const readStats = getArticleReadStats(articles.content);
  const existCategories = categoryCounts.categories.filter(
    (category) => category.count !== 0,
  );

  return (
    <PageLayout activeNav="storage">
      <Container>
        <SideSection>
          <CategoryFilter
            categoryList={[
              {
                value: '전체',
                label: '전체',
                quantity: categoryCounts?.totalCount ?? 0,
              },
              ...(existCategories.map(({ category, count }) => ({
                value: category as CategoryType,
                label: category,
                quantity: count,
              })) ?? []),
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
          <SearchInput
            placeholder="뉴스레터 제목으로 검색하세요..."
            value={searchInput}
            onChange={(e) => {
              setSearchInput(e.target.value);
              debouncedSearch();
            }}
          />
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
          {articles.content.length > 0 ? (
            <ArticleList>
              {articles.content.map((article) => (
                <li key={article.articleId}>
                  <ArticleCard data={article} readVariant="badge" />
                </li>
              ))}
            </ArticleList>
          ) : (
            <EmptyLetterCard title="보관된 뉴스레터가 없어요" />
          )}
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

  width: 100%;
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
