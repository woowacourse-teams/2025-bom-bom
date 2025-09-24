import styled from '@emotion/styled';
import NewsletterFilterContainer from './NewsletterFilterContainer';
import Badge from '@/components/Badge/Badge';
import Tab from '@/components/Tab/Tab';
import { useDevice } from '@/hooks/useDevice';
import { Newsletter } from '@/types/articles';

interface NewsLetterFilterProps {
  newsLetterList: Newsletter['newsletters'];
  selectedNewsletterId: number | null;
  onSelectNewsletter: (id: number | null) => void;
}

function NewsLetterFilter({
  newsLetterList,
  selectedNewsletterId,
  onSelectNewsletter,
}: NewsLetterFilterProps) {
  const device = useDevice();

  return (
    <NewsletterFilterContainer>
      {newsLetterList.map(({ name, articleCount, imageUrl, id }) => (
        <Tab
          key={name}
          value={id ?? null}
          label={name}
          selected={
            selectedNewsletterId === null
              ? name === '전체'
              : id === selectedNewsletterId
          }
          onTabSelect={onSelectNewsletter}
          StartComponent={imageUrl ? <NewsLetterImage src={imageUrl} /> : null}
          EndComponent={<Badge text={String(articleCount)} />}
          textAlign={device === 'pc' ? 'start' : 'center'}
        />
      ))}
    </NewsletterFilterContainer>
  );
}

export default NewsLetterFilter;

const NewsLetterImage = styled.img`
  width: 24px;
  height: 24px;
  border-radius: 50%;

  flex-shrink: 0;

  object-fit: cover;
`;
