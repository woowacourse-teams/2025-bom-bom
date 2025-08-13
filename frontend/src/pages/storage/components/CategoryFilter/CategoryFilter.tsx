import styled from '@emotion/styled';
import Badge from '@/components/Badge/Badge';
import Tab from '@/components/Tab/Tab';
import Tabs from '@/components/Tabs/Tabs';
import { useDeviceType } from '@/hooks/useDeviceType';
import { theme } from '@/styles/theme';
import CategoryIcon from '#/assets/category.svg';

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
  const deviceType = useDeviceType();

  return (
    <Container aria-label="카테고리" isPc={deviceType === 'pc'}>
      {deviceType === 'pc' && (
        <TitleWrapper>
          <IconWrapper>
            <CategoryIcon width={16} height={16} fill={theme.colors.white} />
          </IconWrapper>
          <Title>카테고리</Title>
        </TitleWrapper>
      )}
      <StyledTabs direction={deviceType === 'pc' ? 'vertical' : 'horizontal'}>
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
      </StyledTabs>
    </Container>
  );
}

export default CategoryFilter;

const Container = styled.nav<{ isPc: boolean }>`
  width: 100%;
  padding: 16px;
  border: ${({ isPc, theme }) =>
    isPc ? `1px solid ${theme.colors.stroke}` : 'none'};
  border-radius: 20px;

  display: flex;
  gap: 20px;
  flex-direction: column;
`;

const TitleWrapper = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
`;

const IconWrapper = styled.div`
  padding: 8px;
  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h3`
  font: ${({ theme }) => theme.fonts.heading5};
`;

const StyledTabs = styled(Tabs)`
  padding-bottom: 4px;
  overflow-x: auto;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 2px;
    background: ${({ theme }) => theme.colors.stroke};
  }
`;
