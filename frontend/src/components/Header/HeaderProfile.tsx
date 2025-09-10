import styled from '@emotion/styled';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';
import { DeviceType } from '@/hooks/useDeviceType';
import { UserInfo } from '@/types/me';

interface HeaderProfileProps {
  userInfo: UserInfo;
  deviceType: DeviceType;
  openProfileModal: () => void;
}

const HeaderProfile = ({
  userInfo,
  deviceType,
  openProfileModal,
}: HeaderProfileProps) => {
  if (!userInfo) return null;

  return (
    <Container type="button" onClick={openProfileModal}>
      <ProfileInfo>
        <ProfileImg
          src={userInfo.profileImageUrl ?? ''}
          alt="profile"
          width={32}
          height={32}
        />
        {deviceType !== 'mobile' && (
          <ProfileName>{userInfo.nickname}</ProfileName>
        )}
      </ProfileInfo>
    </Container>
  );
};

export default HeaderProfile;

const Container = styled.button`
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
