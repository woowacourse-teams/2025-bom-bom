import styled from '@emotion/styled';
import CategoryIcon from '../../../../components/icons/CategoryIcon';
import Tab from '../../../../components/Tab/Tab';
import Badge from '../../../../components/Badge/Badge';

interface CategoryItem {
  name: string;
  quantity: number;
}

interface CategoryFilter {
  categoryList: CategoryItem[];
  selectedCategory: string;
  onSelectCategory: (id: string | number) => void;
}

function CategoryFilter({
  categoryList,
  selectedCategory,
  onSelectCategory,
}: CategoryFilter) {
  return (
    <Container aria-label="카테고리">
      <TitleWrapper>
        <IconWrapper>
          <CategoryIcon />
        </IconWrapper>
        <Title>카테고리</Title>
      </TitleWrapper>
      <CategoryTabs>
        <CategoryTab key="전체">
          <Tab
            id="전체"
            selected={selectedCategory === '전체'}
            onSelect={onSelectCategory}
          >
            전체
            <Badge
              text={String(
                categoryList.reduce((acc, cur) => acc + cur.quantity, 0),
              )}
            />
          </Tab>
        </CategoryTab>
        {categoryList.map(({ name, quantity }) => (
          <CategoryTab key={name}>
            <Tab
              id={name}
              selected={selectedCategory === name}
              onSelect={onSelectCategory}
            >
              {name}
              <Badge text={String(quantity)} />
            </Tab>
          </CategoryTab>
        ))}
      </CategoryTabs>
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

const CategoryTabs = styled.ul`
  display: flex;
  flex-direction: column;

  gap: 8px;
`;

const CategoryTab = styled.li``;
