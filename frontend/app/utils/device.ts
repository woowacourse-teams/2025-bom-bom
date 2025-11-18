import * as SecureStore from 'expo-secure-store';
import uuid from 'react-native-uuid';
import type { RNToWebMessage } from '@bombom/shared/webview';

const DEVICE_UUID_KEY = 'deviceUUID';

export const getDeviceUUID = async () => {
  try {
    const deviceUUID = await SecureStore.getItemAsync(DEVICE_UUID_KEY);
    if (!deviceUUID) {
      const newUUID = uuid.v4();
      await SecureStore.setItemAsync(DEVICE_UUID_KEY, newUUID);

      return newUUID;
    }

    return deviceUUID;
  } catch (error) {
    console.error('Device UUID를 가져오는데 실패했습니다.', error);
  }
};

export const sendDeviceInfoToWeb = async (
  sendMessageToWeb: (message: RNToWebMessage) => void,
) => {
  try {
    const deviceUuid = await getDeviceUUID();
    if (deviceUuid) {
      sendMessageToWeb({
        type: 'DEVICE_UUID',
        payload: { deviceUuid },
      });
    }
  } catch (error) {
    console.error('디바이스 정보 전송 실패:', error);
  }
};
