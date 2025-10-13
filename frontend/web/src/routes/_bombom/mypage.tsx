import styled from '@emotion/styled';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { useState } from 'react';
import Button from '@/components/Button/Button';
import ImageWithFallback from '@/components/ImageWithFallback/ImageWithFallback';
import InputField from '@/components/InputField/InputField';
import { toast } from '@/components/Toast/utils/toastActions';
import { queries } from '@/apis/queries';
import { patchNickname } from '@/apis/members';
import { postWithdraw } from '@/apis/auth';
import { useDevice } from '@/hooks/useDevice';
import type { Device } from '@/hooks/useDevice';

export const Route = createFileRoute('/_bombom/mypage')({
  head: () => ({
    meta: [
      {
        title: '봄봄 | 마이페이지',
      },
    ],
  }),
  component: MyPage,
});

function MyPage() {
  const device = useDevice();
  const queryClient = useQueryClient();

  const { data: userInfo } = useQuery(queries.me());
  const { data: myNewsletters } = useQuery(queries.myNewsletters());

  const [nickname, setNickname] = useState(userInfo?.nickname || '');
  const [nicknameError, setNicknameError] = useState<string | null>(null);

  const { mutate: mutateNickname, isPending: isNicknameUpdating } = useMutation(
    {
      mutationKey: ['updateNickname'],
      mutationFn: patchNickname,
      onSuccess: () => {
        toast.success('닉네임이 변경되었습니다.');
        queryClient.invalidateQueries({ queryKey: ['members', 'me'] });
      },
      onError: () => {
        toast.error('닉네임 변경에 실패했습니다.');
      },
    },
  );

  const { mutate: mutateWithdraw } = useMutation({
    mutationKey: ['withdraw'],
    mutationFn: postWithdraw,
    onSuccess: () => {
      window.location.reload();
    },
    onError: () => {
      toast.error('회원탈퇴에 실패했습니다. 다시 시도해주세요.');
    },
  });

  const validateNickname = (value: string) => {
    if (value.length < 2 || value.length > 10) {
      setNicknameError('닉네임은 2자 이상 10자 이하로 입력해주세요.');
      return false;
    }
    setNicknameError(null);
    return true;
  };

  const handleNicknameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setNickname(value);
    validateNickname(value);
  };

  const handleNicknameUpdate = () => {
    if (!validateNickname(nickname)) return;
    if (nickname === userInfo?.nickname) {
      toast.info('변경할 닉네임을 입력해주세요.');
      return;
    }
    mutateNickname({ nickname });
  };

  const handleWithdrawClick = () => {
    const confirmWithdraw = confirm(
      '회원 탈퇴 시, 회원님의 모든 정보가 삭제됩니다. 정말 탈퇴하시겠습니까?',
    );

    if (confirmWithdraw) {
      mutateWithdraw();
    }
  };

  if (!userInfo) return null;

  return (
    <Container device={device}>
      <Title device={device}>마이페이지</Title>

      <Section device={device}>
        <SectionTitle device={device}>닉네임 변경</SectionTitle>
        <NicknameWrapper>
          <InputField
            name="nickname"
            label="닉네임"
            inputValue={nickname}
            onInputChange={handleNicknameChange}
            placeholder="2자 이상 10자 이하로 입력해주세요"
            errorString={nicknameError}
          />
          <Button
            text="변경"
            onClick={handleNicknameUpdate}
            disabled={isNicknameUpdating || !!nicknameError}
          />
        </NicknameWrapper>
      </Section>

      <Divider />

      <Section device={device}>
        <SectionTitle device={device}>구독 뉴스레터</SectionTitle>
        {myNewsletters && myNewsletters.length > 0 ? (
          <NewsletterGrid device={device}>
            {myNewsletters.map((newsletter) => (
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
      </Section>

      <Divider />

      <Section device={device}>
        <WithdrawButton type="button" onClick={handleWithdrawClick}>
          회원 탈퇴
        </WithdrawButton>
      </Section>
    </Container>
  );
}

const Container = styled.div<{ device: Device }>`
  width: 100%;
  max-width: ${({ device }) => (device === 'mobile' ? '100%' : '800px')};
  margin: 0 auto;
  padding: ${({ device }) =>
    device === 'mobile' ? '16px' : device === 'tablet' ? '24px' : '32px'};

  display: flex;
  gap: 24px;
  flex-direction: column;
`;

const Title = styled.h1<{ device: Device }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, device }) =>
    device === 'mobile' ? theme.fonts.heading3 : theme.fonts.heading2};
`;

const Section = styled.section<{ device: Device }>`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const SectionTitle = styled.h2<{ device: Device }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, device }) =>
    device === 'mobile' ? theme.fonts.heading5 : theme.fonts.heading4};
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;
  margin: 8px 0;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const NicknameWrapper = styled.div`
  display: flex;
  gap: 12px;
  align-items: flex-end;
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
  display: flex;
  gap: 4px;
  flex: 1;
  flex-direction: column;

  overflow: hidden;
`;

const NewsletterName = styled.h3`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
  font-weight: 600;

  overflow: hidden;

  text-overflow: ellipsis;
  white-space: nowrap;
`;

const NewsletterDescription = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  overflow: hidden;

  display: -webkit-box;

  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
`;

const EmptyMessage = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
`;

const WithdrawButton = styled.button`
  width: fit-content;

  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};

  &:hover {
    text-decoration: underline;
  }
`;
