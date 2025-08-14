import styled from '@emotion/styled';
import CategoryFilter from '../CategoryFilter/CategoryFilter';
import { CategoryType } from '@/constants/category';
import { components } from '@/types/openapi';

type CategoryCount =
  components['schemas']['GetArticleCountPerCategoryResponse'];

interface CategoryFilterWithCountProps {
  selectedCategory: CategoryType;
  onCategoryChange: (value: CategoryType) => void;
  totalCount: number;
  existCategories: CategoryCount[] | undefined;
}

export default function CategoryFilterWithCount({
  selectedCategory,
  onCategoryChange,
  totalCount,
  existCategories,
}: CategoryFilterWithCountProps) {
  return (
    <CategoryFilterWrapper>
      <CategoryFilter
        categoryList={[
          {
            value: '전체',
            label: '전체',
            quantity: totalCount,
          },
          ...(existCategories?.map(({ category, count }) => ({
            value: category as CategoryType,
            label: category ?? '',
            quantity: count ?? 0,
          })) ?? []),
        ]}
        selectedValue={selectedCategory}
        onSelectCategory={onCategoryChange}
      />
    </CategoryFilterWrapper>
  );
}

const CategoryFilterWrapper = styled.div`
  width: 100%;
  margin-bottom: 8px;
`;
