import styled from '@emotion/styled';
import clockIcon from '#/assets/clock.svg';
import Badge from '@/components/Badge/Badge';
import { Article } from '@/pages/today/types/article';

interface NewsletterItemCardProps {
  data: Article;
}

export default function NewsletterItemCard({ data }: NewsletterItemCardProps) {
  const {
    title,
    contentsSummary,
    thumbnailUrl,
    expectedReadTime,
    newsletter: {
      name: newsletterName,
      category: newsletterCategory,
      imageUrl: newsletterImageUrl,
    },
  } = data;

  return (
    <Container>
      <ImageWrapper>
        <NewsletterImage src={thumbnailUrl ?? newsletterImageUrl} alt={title} />
      </ImageWrapper>

      <ContentWrapper>
        <TextContent>
          <Title>{title}</Title>
          <Description>{contentsSummary}</Description>
        </TextContent>

        <MetaContent>
          <Badge text={newsletterCategory} />
          <MetaInfo>
            <SourceText>from {newsletterName}</SourceText>
            <ReadTimeBox>
              <img src={clockIcon} alt="시계 아이콘" />
              <SourceText>{`${expectedReadTime}분`}</SourceText>
            </ReadTimeBox>
          </MetaInfo>
        </MetaContent>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div`
  overflow: hidden;

  display: flex;
  flex-direction: column;

  width: 100%;
  max-width: 320px;
  border-radius: 20px;
  box-shadow:
    0 4px 6px -1px rgb(0 0 0 / 10%),
    0 2px 4px -1px rgb(0 0 0 / 6%);

  background: ${({ theme }) => theme.colors.white};

  transition: all 0.2s ease;
`;

const ImageWrapper = styled.div`
  overflow: hidden;
  height: 120px;
`;

const NewsletterImage = styled.img`
  width: 100%;

  object-fit: cover;
  object-position: center;
`;

const ContentWrapper = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;

  padding: 20px;
`;

const TextContent = styled.div`
  margin-bottom: 16px;
`;

const Title = styled.h3`
  overflow: hidden;

  display: -webkit-box;

  margin-bottom: 8px;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  text-overflow: ellipsis;
`;

const Description = styled.p`
  overflow: hidden;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  text-overflow: ellipsis;
`;

const MetaContent = styled.div`
  display: flex;
  align-items: center;

  gap: 8px;
`;

const MetaInfo = styled.div`
  display: flex;
  align-items: center;

  gap: 8px;
`;

const SourceText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  align-items: center;

  white-space: nowrap;

  gap: 4px;
`;
