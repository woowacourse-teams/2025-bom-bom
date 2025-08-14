import styled from '@emotion/styled';
import CategoryFilter from '../CategoryFilter/CategoryFilter';
import { GetArticlesStatisticsNewslettersResponse } from '@/apis/articles';
import { CategoryType } from '@/constants/category';

type NewsletterCount = GetArticlesStatisticsNewslettersResponse['newsletters'];

interface CategoryFilterWithCountProps {
  selectedCategory: CategoryType;
  onCategoryChange: (value: CategoryType) => void;
  totalCount: number;
  existNewsletters: NewsletterCount | undefined;
}

export default function CategoryFilterWithCount({
  selectedCategory,
  onCategoryChange,
  totalCount,
  existNewsletters,
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
          ...(existNewsletters?.map(({ newsletter, count }) => ({
            value: newsletter as CategoryType,
            label: newsletter ?? '',
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
