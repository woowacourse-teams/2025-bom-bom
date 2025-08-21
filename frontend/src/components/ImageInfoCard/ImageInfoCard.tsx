import styled from '@emotion/styled';
import { ComponentProps } from 'react';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';

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

const Container = styled.div`
  width: 100%;
  padding: 0;

  display: flex;
  gap: 12px;
  align-items: center;

  text-align: left;
`;

const Image = styled(ImageWithFallback)`
  width: 64px;
  height: 64px;
  border-radius: 16px;

  flex-shrink: 0;

  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
`;

const InfoBox = styled.div`
  flex: 1;
  min-height: 64px;

  display: flex;
  gap: 8px;
  flex-direction: column;
  justify-content: center;
`;

const Title = styled.h3`
  margin: 0;
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading6};
  line-height: 1.3;
`;

const Description = styled.p`
  margin: 0;
  overflow: hidden;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
  line-height: 1.4;

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;
