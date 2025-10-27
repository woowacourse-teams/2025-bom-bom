import styled from '@emotion/styled';
import { sendMessageToRN } from '@/libs/webview/webview.utils';

const NotificationSettingsSection = () => {
  const handleToggleClick = () => {
    sendMessageToRN({ type: 'OPEN_NOTIFICATION_SETTINGS' });
  };

  return (
    <Container>
      <SettingOption>
        <SettingLabel>새로운 아티클 알림 받기</SettingLabel>
        <ToggleWrapper type="button" onClick={handleToggleClick}>
          <ToggleTrack>
            <ToggleThumb />
          </ToggleTrack>
        </ToggleWrapper>
      </SettingOption>

      <InfoText>알림을 수신하려면 앱 설정의 알림 권한이 필요합니다.</InfoText>
    </Container>
  );
};

export default NotificationSettingsSection;

const Container = styled.div`
  width: 100%;

  display: flex;
  gap: 24px;
  flex-direction: column;
`;

const SettingOption = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;

  background-color: ${({ theme }) => theme.colors.white};
`;

const SettingLabel = styled.p`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.body1};
`;

const ToggleWrapper = styled.button`
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.8;
  }

  &:active {
    opacity: 0.6;
  }
`;

const ToggleTrack = styled.div`
  position: relative;
  width: 51px;
  height: 31px;
  border-radius: 16px;

  background-color: ${({ theme }) => theme.colors.disabledText};

  transition: background-color 0.3s;
`;

const ToggleThumb = styled.div`
  position: absolute;
  top: 3px;
  left: 3px;
  width: 25px;
  height: 25px;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgb(0 0 0 / 20%);

  background-color: ${({ theme }) => theme.colors.white};

  transition: transform 0.3s;
`;

const InfoText = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;
