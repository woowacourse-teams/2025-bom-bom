import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import { queries } from '@/apis/queries';
import { DeviceType } from '@/hooks/useDeviceType';
import { copyToClipboard } from '@/utils/copy';
import defaultImage from '#/assets/bombom.png';
import CopyIcon from '#/assets/copy.svg';

interface HeaderProfileProps {
  deviceType: DeviceType;
}

const HeaderProfile = ({ deviceType }: HeaderProfileProps) => {
  const navigate = useNavigate();
  const { data: userInfo, isFetching } = useQuery(queries.me());

  const handleCopyEmail = () => {
    if (!userInfo?.email) return;

    alert(`이메일이 복사되었습니다.`);
    copyToClipboard(userInfo?.email);
  };

  const isLoggedIn = isFetching || userInfo;

  return (
    <Container>
      {isLoggedIn ? (
        <ProfileInfo>
          {deviceType !== 'mobile' && (
            <ProfileImg
              src={userInfo?.profileImageUrl ?? defaultImage}
              alt="profile"
              width={32}
              height={32}
            />
          )}
          <ProfileTextBox>
            {deviceType !== 'mobile' && (
              <ProfileName>{userInfo?.nickname}</ProfileName>
            )}
            <ProfileEmail onClick={handleCopyEmail}>
              <EmailText>{userInfo?.email}</EmailText>
              <CopyIcon width={16} height={16} />
            </ProfileEmail>
          </ProfileTextBox>
        </ProfileInfo>
      ) : (
        <Button
          text="로그인"
          onClick={() => {
            navigate({ to: '/login' });
          }}
        />
      )}
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

const ProfileImg = styled.img`
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

const ProfileName = styled.div``;

const ProfileEmail = styled.div`
  display: flex;
  gap: 4px;
  align-items: center;

  cursor: pointer;
`;

const EmailText = styled.div`
  font: ${({ theme }) => theme.fonts.caption};
`;
