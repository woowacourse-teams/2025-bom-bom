import { theme } from '@bombom/shared/theme';
import styled from '@emotion/styled';
import Badge from '../Badge/Badge';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';
import type { ComponentProps } from 'react';
import CheckIcon from '#/assets/svg/check-circle.svg';

interface ImageInfoCardProps extends ComponentProps<'div'> {
  imageUrl: string;
  title: string;
  description: string;
  isSubscribed: boolean;
  as?: React.ElementType;
}

function ImageInfoCard({
  imageUrl,
  title,
  description,
  isSubscribed,
  as = 'div',
  ...props
}: ImageInfoCardProps) {
  return (
    <Container as={as} {...props}>
      <Image src={imageUrl} alt={`${title} 뉴스레터 이미지`} />
      <InfoBox>
        <TitleBox>
          <Title>{title}</Title>
          {isSubscribed && (
            <Badge
              text="구독 중"
              variant="outlinePrimary"
              icon={
                <CheckIcon width={16} height={16} fill={theme.colors.white} />
              }
            />
          )}
        </TitleBox>
        <Description>{description || title}</Description>
      </InfoBox>
    </Container>
  );
}

export default ImageInfoCard;

export const Container = styled.div`
  width: 100%;
  padding: 0;

  display: flex;
  gap: 12px;
  align-items: center;

  text-align: left;

  box-sizing: border-box;
`;

const Image = styled(ImageWithFallback)`
  width: 64px;
  height: 64px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 10%);

  flex-shrink: 0;

  object-fit: cover;
`;

export const InfoBox = styled.div`
  min-height: 64px;

  display: flex;
  gap: 8px;
  flex: 1;
  flex-direction: column;
  justify-content: center;
`;

const TitleBox = styled.div`
  display: flex;
  gap: 8px;
  flex: 1;
  flex-direction: row;
  align-items: center;
`;

const Title = styled.h3`
  margin: 0;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading6};
`;

const Description = styled.p`
  overflow: hidden;
  margin: 0;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;
