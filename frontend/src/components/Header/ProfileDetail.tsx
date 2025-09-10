import styled from '@emotion/styled';
import { UserInfo } from '@/types/me';
import { copyToClipboard } from '@/utils/copy';
import CopyIcon from '#/assets/copy.svg';

interface ProfileDetailProps {
  userInfo: UserInfo;
}

const ProfileDetail = ({ userInfo }: ProfileDetailProps) => {
  const handleCopyEmail = () => {
    if (!userInfo.email) return;

    copyToClipboard(userInfo.email);
    alert(`이메일이 복사되었습니다.`);
  };

  if (!userInfo) return null;

  return (
    <Content>
      <ProfileImg
        src={userInfo.profileImageUrl ?? ''}
        alt="profile"
        width={36}
        height={36}
      />
      <ProfileTextBox>
        <ProfileName>{userInfo.nickname}</ProfileName>
        <ProfileEmail onClick={handleCopyEmail}>
          <EmailText>{userInfo.email}</EmailText>
          <CopyIcon width={16} height={16} />
        </ProfileEmail>
      </ProfileTextBox>
    </Content>
  );
};

export default ProfileDetail;

const Content = styled.section``;

const ProfileImg = styled.img`
  width: 34px;
  height: 34px;
  border-radius: 50%;
`;

const ProfileTextBox = styled.div`
  display: flex;
  flex-direction: column;
`;

const ProfileName = styled.div`
  color: ${({ theme }) => theme.colors.textPrimary};
`;

const ProfileEmail = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;

  cursor: pointer;
`;

const EmailText = styled.div`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;
