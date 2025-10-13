import styled from '@emotion/styled';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';
import type { ComponentProps } from 'react';

interface ImageInfoCardProps extends ComponentProps<'div'> {
  imageUrl: string;
  title: string;
  description: string;
  as?: React.ElementType;
}

function ImageInfoCard({
  imageUrl,
  title,
  description,
  as = 'div',
  ...props
}: ImageInfoCardProps) {
  return (
    <Container as={as} {...props}>
      <Image src={imageUrl} alt={`${title} 뉴스레터 이미지`} />
      <InfoBox>
        <Title>{title}</Title>
        <Description>{description || title}</Description>
      </InfoBox>
    </Container>
  );
}

export default ImageInfoCard;

export const Container = styled.div`
  width: 100%;
  height: 80px;
  padding: 8px;
  box-sizing: border-box;
  display: flex;
  gap: 12px;
  align-items: center;

  text-align: left;
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
