import styled from '@emotion/styled';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import type { GetMyNewslettersResponse } from '@/apis/members';
import type { Device } from '@/hooks/useDevice';

interface SubscribedNewslettersSectionProps {
  newsletters: GetMyNewslettersResponse;
  device: Device;
}

const SubscribedNewslettersSection = ({
  newsletters,
  device,
}: SubscribedNewslettersSectionProps) => {
  return (
    <Wrapper>
      {newsletters && newsletters.length > 0 ? (
        <NewsletterGrid device={device}>
          {newsletters.map((newsletter) => (
            <NewsletterCard key={newsletter.newsletterId} device={device}>
              <NewsletterImage
                src={newsletter.imageUrl ?? ''}
                alt={newsletter.name}
                width={60}
                height={60}
              />
              <NewsletterInfo>
                <NewsletterName>{newsletter.name}</NewsletterName>
                <NewsletterDescription>
                  {newsletter.description}
                </NewsletterDescription>
              </NewsletterInfo>
            </NewsletterCard>
          ))}
        </NewsletterGrid>
      ) : (
        <EmptyMessage>구독 중인 뉴스레터가 없습니다.</EmptyMessage>
      )}
    </Wrapper>
  );
};

export default SubscribedNewslettersSection;

const Wrapper = styled.div`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const NewsletterGrid = styled.div<{ device: Device }>`
  display: grid;
  gap: 16px;

  grid-template-columns: ${({ device }) =>
    device === 'mobile' ? '1fr' : 'repeat(auto-fill, minmax(300px, 1fr))'};
`;

const NewsletterCard = styled.div<{ device: Device }>`
  padding: 16px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;

  display: flex;
  gap: 12px;
  align-items: center;

  background: ${({ theme }) => theme.colors.white};

  transition: all 0.2s ease-in-out;

  &:hover {
    box-shadow: 0 4px 12px 0 rgb(0 0 0 / 10%);
  }
`;

const NewsletterImage = styled(ImageWithFallback)`
  width: 60px;
  height: 60px;
  border-radius: 8px;

  flex-shrink: 0;

  object-fit: cover;
`;

const NewsletterInfo = styled.div`
  overflow: hidden;

  display: flex;
  gap: 4px;
  flex: 1;
  flex-direction: column;
`;

const NewsletterName = styled.h3`
  overflow: hidden;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
  font-weight: 600;
  white-space: nowrap;

  text-overflow: ellipsis;
`;

const NewsletterDescription = styled.p`
  overflow: hidden;

  display: -webkit-box;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;

const EmptyMessage = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;
