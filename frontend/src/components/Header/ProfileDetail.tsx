import styled from '@emotion/styled';
import { UserInfo } from '@/types/me';
import { copyToClipboard } from '@/utils/copy';
import CopyIcon from '#/assets/copy.svg';
import MailIcon from '#/assets/mail.svg';

interface ProfileDetailProps {
  userInfo: UserInfo;
}

const ProfileDetail = ({ userInfo }: ProfileDetailProps) => {
  const handleCopyEmail = () => {
    if (!userInfo.email) return;

    copyToClipboard(userInfo.email);
    alert(`이메일이 복사되었습니다.`);
  };

  const handleResignClick = () => {
    alert(
      '회원 탈퇴 시, 회원님의 모든 정보가 삭제됩니다. 정말 탈퇴하시겠습니까?',
    );
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
        <ProfileEmail onClick={handleCopyEmail}>
          <EmailText>{userInfo.email}</EmailText>
          <CopyIcon width={16} height={16} />
        </ProfileEmail>
      </EmailWrapper>

      <Divider />

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

const ProfileImg = styled.img`
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

const ProfileEmail = styled.div`
  padding: 4px 8px;
  border-radius: 8px;

  display: flex;
  gap: 4px;
  align-items: center;

  background: ${({ theme }) => theme.colors.dividers};

  cursor: pointer;
`;

const EmailText = styled.div`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
`;

const ResignButton = styled.button`
  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.body3};
`;
