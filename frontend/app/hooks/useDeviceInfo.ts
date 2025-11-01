import { useWebView } from '@/contexts/WebViewContext';
import { getDeviceUUID } from '@/utils/device';

export const useDeviceInfo = () => {
  const { sendMessageToWeb } = useWebView();

  const sendDeviceInfoToWeb = async () => {
    try {
      const deviceUuid = await getDeviceUUID();
      if (deviceUuid) {
        sendMessageToWeb({
          type: 'DEVICE_INFO',
          payload: { deviceUuid },
        });
      }
    } catch (error) {
      console.error('디바이스 정보 전송 실패:', error);
    }
  };

  return {
    sendDeviceInfoToWeb,
  };
};
