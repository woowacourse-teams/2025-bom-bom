import styled from '@emotion/styled';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';
import { toast } from '../Toast/utils/toastActions';
import { postLogout, postWithdraw } from '@/apis/auth';
import { useDevice } from '@/hooks/useDevice';
import { showMessenger } from '@/libs/channelTalk/channelTalk.utils';
import { copyToClipboard } from '@/utils/copy';
import type { UserInfo } from '@/types/me';
import ChatIcon from '#/assets/svg/chat.svg';
import CopyIcon from '#/assets/svg/copy.svg';
import LogoutIcon from '#/assets/svg/logout.svg';
import MailIcon from '#/assets/svg/mail.svg';

interface ProfileDetailProps {
  userInfo: UserInfo;
}

const ProfileDetail = ({ userInfo }: ProfileDetailProps) => {
  const navigate = useNavigate();
  const deviceType = useDevice();

  const { mutate: mutateLogout } = useMutation({
    mutationKey: ['logout'],
    mutationFn: postLogout,
    onSuccess: () => {
      window.location.reload();
    },
    onError: () => {
      toast.error('로그아웃에 실패했습니다.');
    },
  });

  const { mutate: mutateWithdraw } = useMutation({
    mutationKey: ['withdraw'],
    mutationFn: postWithdraw,
    onSuccess: () => {
      navigate({ to: '/recommend' });
    },
    onError: () => {
      toast.error('회원탈퇴에 실패했습니다. 다시 시도해주세요.');
    },
  });

  const handleCopyEmail = () => {
    if (!userInfo.email) return;

    copyToClipboard(userInfo.email);
    toast.success('이메일이 복사되었습니다.');
  };

  const handleResignClick = () => {
    const confirmWithdraw = confirm(
      '회원 탈퇴 시, 회원님의 모든 정보가 삭제됩니다. 정말 탈퇴하시겠습니까?',
    );

    if (confirmWithdraw) {
      mutateWithdraw();
    }
  };

  const handleLogoutClick = () => {
    mutateLogout();
  };

  if (!userInfo) return null;

  return (
    <Container>
      <Profile>
        <ProfileImg
          src={userInfo.profileImageUrl ?? ''}
          alt="profile"
          width={36}
          height={36}
        />
        <ProfileName>{userInfo.nickname}</ProfileName>
      </Profile>

      <Divider />

      <EmailWrapper>
        <EmailTitleWrapper>
          <StyledMailIcon />
          <EmailTitle>봄봄 구독 이메일</EmailTitle>
        </EmailTitleWrapper>
        <ProfileEmail type="button" onClick={handleCopyEmail}>
          <EmailText>{userInfo.email}</EmailText>
          <CopyIcon width={16} height={16} />
        </ProfileEmail>
      </EmailWrapper>

      <Divider />

      {deviceType !== 'pc' && (
        <ChannelTalkButton type="button" onClick={showMessenger}>
          <ChatIcon width={16} height={16} />
          문의하기
        </ChannelTalkButton>
      )}

      <LogoutButton type="button" onClick={handleLogoutClick}>
        <LogoutIcon width={16} height={16} />
        로그아웃
      </LogoutButton>

      <ResignButton type="button" onClick={handleResignClick}>
        회원 탈퇴
      </ResignButton>
    </Container>
  );
};

export default ProfileDetail;

const Container = styled.div`
  display: flex;
  gap: 16px;
  flex-direction: column;
`;

const Profile = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const ProfileImg = styled(ImageWithFallback)`
  width: 34px;
  height: 34px;
  border-radius: 50%;
`;

const Divider = styled.div`
  width: 100%;
  height: 1px;
  margin: 4px 0;

  background-color: ${({ theme }) => theme.colors.dividers};
`;

const ProfileName = styled.div`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const EmailWrapper = styled.div`
  display: flex;
  gap: 8px;
  flex-direction: column;
`;

const EmailTitleWrapper = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;

  text-align: center;
`;

const StyledMailIcon = styled(MailIcon)`
  width: 14px;
  height: 14px;

  color: ${({ theme }) => theme.colors.textSecondary};
`;

const EmailTitle = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ProfileEmail = styled.button`
  padding: 4px 8px;
  border-radius: 8px;

  display: flex;
  gap: 4px;
  align-items: center;

  background: ${({ theme }) => theme.colors.dividers};

  &:hover {
    background: ${({ theme }) => theme.colors.stroke};
    transition: all 0.2s ease-in-out;
  }
`;

const EmailText = styled.div`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ChannelTalkButton = styled.button`
  display: flex;
  gap: 4px;
  align-items: center;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};

  &:hover {
    text-decoration: underline;
    transition: all 0.2s ease-in-out;
  }
`;

const LogoutButton = styled.button`
  display: flex;
  gap: 4px;
  align-items: center;

  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};

  &:hover {
    text-decoration: underline;
    transition: all 0.2s ease-in-out;
  }
`;

const ResignButton = styled.button`
  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};
`;
