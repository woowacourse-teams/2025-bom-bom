import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { useState, useEffect } from 'react';
import useNotificationMutation from './useNotificationMutation';
import { queries } from '@/apis/queries';
import { useUserInfo } from '@/hooks/useUserInfo';
import {
  sendMessageToRN,
  addWebViewMessageListener,
} from '@/libs/webview/webview.utils';

const NotificationSettingsSection = () => {
  const [deviceUuid, setDeviceUuid] = useState<string>('');
  const { userInfo } = useUserInfo();

  const { data: notificationStatus } = useQuery(
    queries.notificationStatus({
      memberId: userInfo?.id ?? 0,
      deviceUuid,
    }),
  );

  useEffect(() => {
    sendMessageToRN({ type: 'REQUEST_DEVICE_UUID' });

    const unsubscribe = addWebViewMessageListener((message) => {
      if (message.type === 'GET_DEVICE_UUID') {
        setDeviceUuid(message.payload.deviceUuid);
      }
    });

    return unsubscribe;
  }, []);

  const { mutate: updateNotificationSettings } = useNotificationMutation({
    memberId: userInfo?.id ?? 0,
    deviceUuid,
  });

  const handleToggleClick = () => {
    if (!userInfo?.id || !deviceUuid) return;

    const newStatus = !notificationStatus;

    sendMessageToRN({
      type: 'CHECK_NOTIFICATION_PERMISSION',
      payload: { enabled: newStatus },
    });

    updateNotificationSettings(newStatus);
  };

  return (
    <Container>
      <SettingOption>
        <SettingLabel>새로운 아티클 알림 받기</SettingLabel>
        <ToggleWrapper type="button" onClick={handleToggleClick}>
          <ToggleTrack enabled={notificationStatus ?? false}>
            <ToggleThumb enabled={notificationStatus ?? false} />
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

const ToggleTrack = styled.div<{ enabled: boolean }>`
  position: relative;
  width: 51px;
  height: 31px;
  border-radius: 16px;

  background-color: ${({ theme, enabled }) =>
    enabled ? theme.colors.primary : theme.colors.disabledText};

  transition: background-color 0.3s;
`;

const ToggleThumb = styled.div<{ enabled: boolean }>`
  position: absolute;
  top: 3px;
  left: 3px;
  width: 25px;
  height: 25px;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgb(0 0 0 / 20%);

  background-color: ${({ theme }) => theme.colors.white};

  transform: translateX(${({ enabled }) => (enabled ? '20px' : '0')});
  transition: transform 0.3s;
`;

const InfoText = styled.p`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.caption};
`;
