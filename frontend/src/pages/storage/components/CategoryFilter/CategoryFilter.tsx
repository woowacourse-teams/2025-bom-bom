import styled from '@emotion/styled';
import Badge from '@/components/Badge/Badge';
import CategoryIcon from '@/components/icons/CategoryIcon';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';

interface CategoryItem<T extends string> {
  value: T;
  label: string;
  quantity: number;
}

interface CategoryFilterProps<T extends string> {
  categoryList: CategoryItem<T>[];
  selectedValue: T;
  onSelectCategory: (value: T) => void;
}

function CategoryFilter<T extends string>({
  categoryList,
  selectedValue,
  onSelectCategory,
}: CategoryFilterProps<T>) {
  return (
    <Container aria-label="카테고리">
      <TitleWrapper>
        <IconWrapper>
          <CategoryIcon />
        </IconWrapper>
        <Title>카테고리</Title>
      </TitleWrapper>
      <Tabs direction="vertical">
        {categoryList.map(({ value, label, quantity }) => (
          <Tab
            key={value}
            value={value}
            label={label}
            selected={selectedValue === value}
            onTabSelect={onSelectCategory}
            EndComponent={<Badge text={String(quantity)} />}
          />
        ))}
      </Tabs>
    </Container>
  );
}

export default CategoryFilter;

const Container = styled.nav`
  display: flex;
  gap: 20px;
  flex-direction: column;

  width: 310px;
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 20px;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
`;

const IconWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  padding: 8px;
  border-radius: 50%;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading5};
`;
