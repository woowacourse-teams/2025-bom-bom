import styled from '@emotion/styled';
import { useState, useEffect } from 'react';
import {
  sendMessageToRN,
  addWebViewMessageListener,
} from '@/libs/webview/webview.utils';

const NotificationSettingsSection = () => {
  const [isEnabled, setIsEnabled] = useState(false);

  useEffect(() => {
    sendMessageToRN({ type: 'REQUEST_NOTIFICATION_STATUS' });

    const unsubscribe = addWebViewMessageListener((message) => {
      if (message.type === 'NOTIFICATION_STATUS') {
        setIsEnabled(message.payload.enabled);
      }
    });

    return unsubscribe;
  }, []);

  const handleToggleClick = () => {
    const newEnabled = !isEnabled;
    sendMessageToRN({
      type: 'TOGGLE_NOTIFICATION',
      payload: { enabled: newEnabled },
    });
  };

  return (
    <Container>
      <SettingOption>
        <SettingLabel>새로운 아티클 알림 받기</SettingLabel>
        <ToggleWrapper type="button" onClick={handleToggleClick}>
          <ToggleTrack isEnabled={isEnabled}>
            <ToggleThumb isEnabled={isEnabled} />
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

const ToggleWrapper = styled.button<{ disabled?: boolean }>`
  cursor: ${({ disabled }) => (disabled ? 'not-allowed' : 'pointer')};
  opacity: ${({ disabled }) => (disabled ? 0.5 : 1)};
  transition: opacity 0.2s;

  &:hover:not(:disabled) {
    opacity: 0.8;
  }

  &:active:not(:disabled) {
    opacity: 0.6;
  }
`;

const ToggleTrack = styled.div<{ isEnabled: boolean }>`
  position: relative;
  width: 51px;
  height: 31px;
  border-radius: 16px;

  background-color: ${({ theme, isEnabled }) =>
    isEnabled ? theme.colors.primary : theme.colors.disabledText};

  transition: background-color 0.3s;
`;

const ToggleThumb = styled.div<{ isEnabled: boolean }>`
  position: absolute;
  top: 3px;
  left: 3px;
  width: 25px;
  height: 25px;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgb(0 0 0 / 20%);

  background-color: ${({ theme }) => theme.colors.white};

  transform: translateX(${({ isEnabled }) => (isEnabled ? '20px' : '0')});
  transition: transform 0.3s;
`;

const InfoText = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;
