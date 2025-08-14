import styled from '@emotion/styled';
import CategoryFilter from '../CategoryFilter/CategoryFilter';
import { GetArticlesStatisticsNewslettersResponse } from '@/apis/articles';

type NewsletterCount = GetArticlesStatisticsNewslettersResponse['newsletters'];

interface NewsletterFilterWithCountProps {
  selectedNewsletter: string;
  onNewsletterChange: (value: string) => void;
  totalCount: number;
  existNewsletters: NewsletterCount | undefined;
}

export default function NewsletterFilterWithCount({
  selectedNewsletter,
  onNewsletterChange,
  totalCount,
  existNewsletters,
}: NewsletterFilterWithCountProps) {
  return (
    <NewsletterFilterWrapper>
      <CategoryFilter
        newsLetterList={[
          {
            newsletter: '전체',
            count: totalCount,
            imageUrl: '',
          },
          ...(existNewsletters ?? []),
        ]}
        selectedValue={selectedNewsletter}
        onSelectNewsletter={onNewsletterChange}
      />
    </NewsletterFilterWrapper>
  );
}

const NewsletterFilterWrapper = styled.div`
  width: 100%;
  margin-bottom: 8px;
`;
