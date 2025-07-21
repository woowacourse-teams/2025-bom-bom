import styled from '@emotion/styled';

interface ImageInfoCardProps {
  imageUrl: string;
  name: string;
  description: string;
}

function ImageInfoCard({ imageUrl, name, description }: ImageInfoCardProps) {
  return (
    <Container>
      <Image src={imageUrl} alt={`${name} 뉴스레터 이미지`} />
      <InfoBox>
        <Title>{name}</Title>
        <Description>{description}</Description>
      </InfoBox>
    </Container>
  );
}

export default ImageInfoCard;

const Container = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  width: fit-content;
  padding: 12px 8px;

  gap: 8px;
`;

const Image = styled.img`
  flex-shrink: 0;

  width: 58px;
  border-radius: 12px;

  aspect-ratio: 1/1;
  object-fit: cover;
`;

const InfoBox = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: flex-start;

  height: 60px;

  gap: 4px;
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
