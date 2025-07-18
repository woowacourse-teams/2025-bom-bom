import styled from '@emotion/styled';

interface ImageInfoCardProps {
  imageUrl: string;
  title: string;
  description?: string;
}

function ImageInfoCard({ imageUrl, title, description }: ImageInfoCardProps) {
  return (
    <Container>
      <Image src={imageUrl} alt={`${title} 뉴스레터 이미지`} />
      <InfoBox>
        <Title>{title}</Title>
        {description && <Description>{description}</Description>}
      </InfoBox>
    </Container>
  );
}

export default ImageInfoCard;

const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;

  width: fit-content;
  padding: 12px 8px;
`;

const Image = styled.img`
  width: 58px;
  aspect-ratio: 1/1;
  flex-shrink: 0;
  object-fit: cover;

  border-radius: 12px;
`;

const InfoBox = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 4px;

  height: 60px;
`;

const Title = styled.h3`
  ${({ theme }) => theme.fonts.heading6}
  color: ${({ theme }) => theme.colors.textPrimary};
`;

const Description = styled.p`
  overflow: hidden;

  display: -webkit-box;

  ${({ theme }) => theme.fonts.caption}
  color: ${({ theme }) => theme.colors.textPrimary};
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
`;
