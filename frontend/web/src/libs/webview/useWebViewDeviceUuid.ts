import { useEffect, useState } from 'react';
import {
  sendMessageToRN,
  addWebViewMessageListener,
} from '@/libs/webview/webview.utils';

export const useWebViewDeviceUuid = () => {
  const [deviceUuid, setDeviceUuid] = useState('');

  useEffect(() => {
    sendMessageToRN({ type: 'REQUEST_DEVICE_UUID' });

    const unsubscribe = addWebViewMessageListener((message) => {
      if (message.type === 'DEVICE_UUID') {
        setDeviceUuid(message.payload.deviceUuid);
      }
    });

    return () => unsubscribe();
  }, []);

  return deviceUuid;
};
