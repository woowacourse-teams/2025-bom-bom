import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useState } from 'react';
import SearchInput from '../../components/SearchInput/SearchInput';
import Select from '../../components/Select/Select';
import CategoryFilter from '../../pages/storage/components/CategoryFilter/CategoryFilter';
import { queries } from '@/apis/queries';
import Pagination from '@/components/Pagination/Pagination';
import { CategoryType } from '@/constants/category';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import QuickMenu from '@/pages/storage/components/QuickMenu/QuickMenu';
import ArticleCard from '@/pages/today/components/ArticleCard/ArticleCard';
import EmptyLetterCard from '@/pages/today/components/EmptyLetterCard/EmptyLetterCard';
import { theme } from '@/styles/theme';
import StorageIcon from '#/assets/storage.svg';

export const Route = createFileRoute('/_bombom/storage')({
  component: Storage,
});

function Storage() {
  const [selectedCategory, setSelectedCategory] =
    useState<CategoryType>('전체');
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const [searchInput, setSearchInput] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const debouncedSearchInput = useDebouncedValue(searchInput, 500);

  const { data: articles, isLoading } = useQuery(
    queries.articles({
      sort: `arrivedDateTime,${sortFilter}`,
      category: selectedCategory === '전체' ? undefined : selectedCategory,
      keyword: debouncedSearchInput,
      size: 6,
      page: currentPage - 1,
    }),
  );

  const { data: categoryCounts } = useQuery(
    queries.statisticsCategories({
      keyword: debouncedSearchInput,
    }),
  );

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCategoryChange = (value: CategoryType) => {
    setSelectedCategory(value);
    setCurrentPage(1);
  };

  const handleSortChange = (value: 'DESC' | 'ASC') => {
    setSortFilter(value);
    setCurrentPage(1);
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchInput(e.target.value);
  };

  useEffect(() => {
    setCurrentPage(1);
  }, [debouncedSearchInput]);

  const existCategories = categoryCounts?.categories?.filter(
    (category) => category.count !== 0,
  );
  const isLoadingOrHaveContent =
    isLoading || (articles?.content?.length && articles?.content.length > 0);

  return (
    <Container>
      <MainSection>
        <TitleWrapper>
          <TitleIconBox>
            <StorageIcon color={theme.colors.white} />
          </TitleIconBox>
          <Title>뉴스레터 보관함</Title>
        </TitleWrapper>

        <ContentWrapper>
          <SidebarSection>
            <CategoryFilterWrapper>
              <CategoryFilter
                categoryList={[
                  {
                    value: '전체',
                    label: '전체',
                    quantity: categoryCounts?.totalCount ?? 0,
                  },
                  ...(existCategories?.map(({ category, count }) => ({
                    value: category as CategoryType,
                    label: category ?? '',
                    quantity: count ?? 0,
                  })) ?? []),
                ]}
                selectedValue={selectedCategory}
                onSelectCategory={handleCategoryChange}
              />
            </CategoryFilterWrapper>
            <QuickMenu />
          </SidebarSection>
          <MainContentSection>
            <SearchInput
              placeholder="뉴스레터 제목으로 검색하세요..."
              value={searchInput}
              onChange={handleSearchChange}
            />
            <SummaryBar>
              <SummaryText>총 {articles?.totalElements ?? 0}개</SummaryText>
              <Select
                options={[
                  { value: 'DESC', label: '최신순' },
                  { value: 'ASC', label: '오래된순' },
                ]}
                selectedValue={sortFilter}
                onSelectOption={handleSortChange}
              />
            </SummaryBar>

            {isLoadingOrHaveContent ? (
              <>
                <ArticleList>
                  {articles?.content?.map((article) => (
                    <li key={article.articleId}>
                      <ArticleCard data={article} readVariant="badge" />
                    </li>
                  ))}
                </ArticleList>
                <Pagination
                  currentPage={currentPage}
                  totalPages={articles?.totalPages ?? 1}
                  onPageChange={handlePageChange}
                />
              </>
            ) : (
              <EmptyLetterCard title="보관된 뉴스레터가 없어요" />
            )}
          </MainContentSection>
        </ContentWrapper>
      </MainSection>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 1280px;
  padding: 64px 0;

  display: flex;
  align-items: flex-start;
  justify-content: center;
`;

const MainSection = styled.div`
  width: 100%;

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
  width: 28px;
  height: 28px;
  padding: 6px;
  border-radius: 14px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading2};
`;

const ContentWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 32px;
  align-items: flex-start;

  @media (width <= 1024px) {
    gap: 20px;
    flex-direction: column;
  }
`;

const SidebarSection = styled.div`
  width: 320px;

  display: flex;
  gap: 20px;
  flex-direction: column;

  @media (width <= 1024px) {
    min-width: 100%;
    order: 1;
  }
`;

const MainContentSection = styled.div`
  display: flex;
  gap: 20px;
  flex: 1;
  flex-direction: column;

  @media (width <= 1024px) {
    order: 2;
  }
`;

const CategoryFilterWrapper = styled.div`
  width: 100%;
  margin-bottom: 8px;
`;

const SummaryBar = styled.div`
  width: 100%;

  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const SummaryText = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ArticleList = styled.ul`
  width: 100%;

  display: flex;
  gap: 16px;
  flex-direction: column;
`;
