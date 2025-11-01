import * as SecureStore from 'expo-secure-store';
import uuid from 'react-native-uuid';

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
