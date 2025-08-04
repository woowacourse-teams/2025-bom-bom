import styled from '@emotion/styled';
import Badge from '@/components/Badge/Badge';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import { components } from '@/types/openapi';
import ClockIcon from '#/assets/clock.svg';

interface NewsletterItemCardProps {
  data: components['schemas']['ArticleResponse'];
}

export default function NewsletterItemCard({ data }: NewsletterItemCardProps) {
  const { title, contentsSummary, thumbnailUrl, expectedReadTime, newsletter } =
    data;

  return (
    <Container>
      <NewsletterImage
        src={thumbnailUrl ?? newsletter?.imageUrl ?? ''}
        alt={title ?? ''}
        height={180}
      />

      <ContentWrapper>
        <TextContent>
          <Title>{title}</Title>
          <Description>{contentsSummary}</Description>
        </TextContent>

        <MetaContent>
          <Badge text={newsletter?.category ?? ''} />
          <MetaInfo>
            <SourceText>from {newsletter?.name ?? ''}</SourceText>
            <ReadTimeBox>
              <ClockIcon width={16} height={16} />
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

const NewsletterImage = styled(ImageWithFallback)`
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
  gap: 8px;
  align-items: center;
`;

const MetaInfo = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const SourceText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;

const ReadTimeBox = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;

  white-space: nowrap;
`;
