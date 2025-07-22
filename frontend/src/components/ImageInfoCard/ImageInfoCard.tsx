import styled from '@emotion/styled';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';

interface ImageInfoCardProps {
  imageUrl: string;
  title: string;
  description: string;
}

function ImageInfoCard({ imageUrl, title, description }: ImageInfoCardProps) {
  return (
    <Container>
      <Image src={imageUrl} alt={`${title} 뉴스레터 이미지`} />
      <InfoBox>
        <Title>{title}</Title>
        <Description>{description}</Description>
      </InfoBox>
    </Container>
  );
}

export default ImageInfoCard;

const Container = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;

  width: fit-content;
  padding: 12px 8px;
`;

const Image = styled(ImageWithFallback)`
  flex-shrink: 0;

  width: 58px;
  border-radius: 12px;

  aspect-ratio: 1/1;
  object-fit: cover;
`;

const InfoBox = styled.div`
  display: flex;
  gap: 4px;
  flex-direction: column;
  justify-content: flex-start;

  height: 60px;
`;

const Title = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading6};
`;

const Description = styled.p`
  overflow: hidden;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.caption};

  -webkit-box-orient: vertical;

  -webkit-line-clamp: 2;
`;
