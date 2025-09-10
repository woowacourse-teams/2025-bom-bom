import styled from '@emotion/styled';
import ProfileDetail from './ProfileDetail';
import ImageWithFallback from '../ImageWithFallback/ImageWithFallback';
import Modal from '../Modal/Modal';
import useModal from '../Modal/useModal';
import { DeviceType } from '@/hooks/useDeviceType';
import { UserInfo } from '@/types/me';

interface HeaderProfileProps {
  userInfo: UserInfo;
  deviceType: DeviceType;
}

const HeaderProfile = ({ userInfo, deviceType }: HeaderProfileProps) => {
  const {
    modalRef: profileModalRef,
    openModal: openProfileModal,
    closeModal: closeProfileModal,
    isOpen,
  } = useModal();

  if (!userInfo) return null;

  return (
    <Container>
      <HeaderProfileWrapper type="button" onClick={openProfileModal}>
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
      </HeaderProfileWrapper>
      {userInfo && (
        <Modal
          modalRef={profileModalRef}
          closeModal={closeProfileModal}
          isOpen={isOpen}
          position="dropdown"
          showCloseButton={false}
          showBackdrop={false}
        >
          <ProfileDetail userInfo={userInfo} />
        </Modal>
      )}
    </Container>
  );
};

export default HeaderProfile;

const Container = styled.div`
  display: flex;
  flex-direction: column;
`;

const HeaderProfileWrapper = styled.button`
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
