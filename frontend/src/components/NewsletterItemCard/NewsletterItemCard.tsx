import styled from '@emotion/styled';
import { Article } from '../../pages/today/types/article';

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
    <CardContainer>
      <ImageContainer>
        <NewsletterImage src={thumbnailUrl ?? newsletterImageUrl} alt={title} />
      </ImageContainer>

      <ContentContainer>
        <TextContent>
          <Title>{title}</Title>
          <Description>{contentsSummary}</Description>
        </TextContent>

        <MetaContent>
          <Tag>{newsletterCategory}</Tag>
          <MetaInfo>
            <SourceText>from {newsletterName}</SourceText>
            <ReadTimeContainer>
              <ClockIcon />
              <ReadTime>{`${expectedReadTime}분`}</ReadTime>
            </ReadTimeContainer>
          </MetaInfo>
        </MetaContent>
      </ContentContainer>
    </CardContainer>
  );
}

const CardContainer = styled.div`
  overflow: hidden;

  display: flex;
  flex-direction: column;

  width: 100%;
  max-width: 525px;
  border-radius: 21px;
  box-shadow:
    0 4px 6px -1px rgb(0 0 0 / 10%),
    0 2px 4px -1px rgb(0 0 0 / 6%);

  background: ${({ theme }) => theme.colors.white};

  transition: all 0.2s ease;
`;

const ImageContainer = styled.div`
  overflow: hidden;

  width: 100%;
  height: 224px;
`;

const NewsletterImage = styled.img`
  width: 100%;
  height: 100%;

  object-fit: cover;
  object-position: center;
`;

const ContentContainer = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;

  padding: 21px;
`;

const TextContent = styled.div`
  margin-bottom: 16px;
`;

const Title = styled.h3`
  margin-bottom: 8px;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
  line-height: 28px;
`;

const Description = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
  line-height: 22px;
`;

const MetaContent = styled.div`
  display: flex;
  align-items: center;

  gap: 8px;
`;

const Tag = styled.span`
  display: inline-flex;
  align-items: center;
  justify-content: center;

  padding: 4px 8px;
  border-radius: 8px;

  background: ${({ theme }) => theme.colors.dividers};

  color: ${({ theme }) => theme.colors.textPrimary};
  font:
    500 12px/18px Montserrat,
    'Noto Sans KR',
    sans-serif;
`;

const MetaInfo = styled.div`
  display: flex;
  align-items: center;

  gap: 8px;
`;

const SourceText = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font:
    500 12px/18px Montserrat,
    sans-serif;
`;

const ReadTimeContainer = styled.div`
  display: flex;
  align-items: center;

  gap: 3.5px;
`;

const ClockIcon = styled.div`
  width: 14px;
  height: 14px;

  background: ${({ theme }) => theme.colors.textTertiary};

  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke-width='1.5' stroke='currentColor'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z' /%3E%3C/svg%3E")
    no-repeat center;
  mask-size: contain;
`;

const ReadTime = styled.span`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
`;
