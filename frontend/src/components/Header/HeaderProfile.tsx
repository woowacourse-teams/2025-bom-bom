import styled from '@emotion/styled';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';
import { DeviceType } from '@/hooks/useDeviceType';
import { UserInfo } from '@/types/me';
import { copyToClipboard } from '@/utils/copy';
import CopyIcon from '#/assets/copy.svg';

interface HeaderProfileProps {
  userInfo: UserInfo;
  deviceType: DeviceType;
}

const HeaderProfile = ({ userInfo, deviceType }: HeaderProfileProps) => {
  const handleCopyEmail = () => {
    if (!userInfo?.email) return;

    copyToClipboard(userInfo?.email);
    alert(`이메일이 복사되었습니다.`);
  };

  if (!userInfo) return null;

  return (
    <Container>
      <ProfileInfo>
        {deviceType !== 'mobile' && (
          <ProfileImg
            src={userInfo.profileImageUrl ?? ''}
            alt="profile"
            width={32}
            height={32}
          />
        )}
        <ProfileTextBox>
          {deviceType !== 'mobile' && (
            <ProfileName>{userInfo.nickname}</ProfileName>
          )}
          <ProfileEmail onClick={handleCopyEmail}>
            <EmailText>{userInfo.email}</EmailText>
            <CopyIcon width={16} height={16} />
          </ProfileEmail>
        </ProfileTextBox>
      </ProfileInfo>
    </Container>
  );
};

export default HeaderProfile;

const Container = styled.div`
  min-width: 200px;
  padding: 8px 12px;
  border-radius: 12px;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;

  background: ${({ theme }) => theme.colors.white};
`;

const ProfileImg = styled(ImageWithFallback)`
  width: 34px;
  height: 34px;
  border-radius: 50%;
`;

const ProfileTextBox = styled.div`
  display: flex;
  flex-direction: column;
`;

const ProfileInfo = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;

  font: ${({ theme }) => theme.fonts.caption};
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
