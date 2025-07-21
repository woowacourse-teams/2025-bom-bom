import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useState } from 'react';
import StorageIcon from '../components/icons/StorageIcon';
import PageLayout from '../components/PageLayout/PageLayout';
import SearchInput from '../components/SearchInput/SearchInput';
import Select from '../components/Select/Select';
import { CATEGORIES, CategoryType } from '../constants/category';
import { ARTICLES } from '../mocks/data/mock-articles';
import CategoryFilter from '../pages/archive/components/CategoryFilter/CategoryFilter';
import ArticleCard from '../pages/today/components/ArticleCard';

export const Route = createFileRoute('/storage')({
  component: Storage,
});

function Storage() {
  const [selectedCategory, setSelectedCategory] =
    useState<CategoryType>('전체');
  return (
    <PageLayout activeNav="storage">
      <Container>
        <SideSection>
          <CategoryFilter
            categoryList={CATEGORIES.map((category) => ({
              value: category,
              label: category,
              quantity: 10,
            }))}
            selectedValue={selectedCategory}
            onSelectCategory={(name) => setSelectedCategory(name)}
          />
        </SideSection>
        <MainSection>
          <TitleBox>
            <TitleIconWrapper>
              <StorageIcon color="white" />
            </TitleIconWrapper>
            <Title>뉴스레터 보관함</Title>
          </TitleBox>
          <SearchInput placeholder="뉴스레터 제목이나 발행처로 검색하세요..." />
          <SummaryBar>
            <SummaryText>총 10개 • 읽지 않음 5개 • 읽음 5개</SummaryText>
            <Select
              options={[
                { value: 'desc', label: '최신순' },
                { value: 'asc', label: '오래된순' },
              ]}
              selectedValue={'desc'}
              onSelectOption={() => {}}
            />
          </SummaryBar>
          <ArticleList>
            {ARTICLES.map((article) => (
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
  align-items: flex-start;
  justify-content: center;

  width: 100%;
  max-width: 1280px;
  padding: 64px 0;

  gap: 24px;
`;

const SideSection = styled.div`
  margin-top: 70px;
`;

const MainSection = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  gap: 20px;
`;

const TitleBox = styled.div`
  display: flex;
  align-items: center;

  gap: 8px;
`;

const TitleIconWrapper = styled.div`
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
  flex-direction: column;

  gap: 16px;
`;
