import styled from '@emotion/styled';
import CategoryIcon from '../../../../components/icons/CategoryIcon';
import Tabs from '../../../../components/Tabs/Tabs';
import Tab from '../../../../components/Tabs/Tab';
import Badge from '../../../../components/Badge/Badge';

interface CategoryItem {
  name: string;
  quantity: number;
}

interface CategoryFilterProps {
  categoryList: CategoryItem[];
  selectedCategory: string;
  onSelectCategory: (id: string | number) => void;
}

function CategoryFilter({
  categoryList,
  selectedCategory,
  onSelectCategory,
}: CategoryFilterProps) {
  const totalQuantity = categoryList.reduce(
    (currentQuantity, { quantity }) => currentQuantity + quantity,
    0,
  );

  return (
    <Container aria-label="카테고리">
      <TitleWrapper>
        <IconWrapper>
          <CategoryIcon />
        </IconWrapper>
        <Title>카테고리</Title>
      </TitleWrapper>
      <Tabs direction="vertical">
        <Tab
          id="전체"
          selected={selectedCategory === '전체'}
          onSelect={onSelectCategory}
          text="전체"
          TrailingComponent={<Badge text={String(totalQuantity)} />}
        />
        {categoryList.map(({ name, quantity }) => (
          <Tab
            key={name}
            id={name}
            selected={selectedCategory === name}
            onSelect={onSelectCategory}
            text={name}
            TrailingComponent={<Badge text={String(quantity)} />}
          />
        ))}
      </Tabs>
    </Container>
  );
}

export default CategoryFilter;

const Container = styled.nav`
  display: flex;
  flex-direction: column;

  width: 310px;
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 20px;

  gap: 20px;
`;

const TitleWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-start;

  gap: 10px;
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
