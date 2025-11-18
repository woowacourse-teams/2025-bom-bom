import messaging from '@react-native-firebase/messaging';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import { Alert, Linking, Platform } from 'react-native';

// 안드로이드 알림 채널 생성
export const createAndroidChannel = async () => {
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: '기본 알림',
      importance: Notifications.AndroidImportance.DEFAULT,
    });
  }
};

export const hasRequestedPermission = async (): Promise<boolean> => {
  try {
    const key = 'notification_permission_requested';
    const permissionRequested = await AsyncStorage.getItem(key);
    return permissionRequested === 'true';
  } catch (error) {
    console.error('권한 요청 기록 확인 실패:', error);
    return false;
  }
};

const setPermissionRequested = async (): Promise<void> => {
  try {
    const key = 'notification_permission_requested';
    await AsyncStorage.setItem(key, 'true');
  } catch (error) {
    console.error('권한 요청 기록 저장 실패:', error);
  }
};

// 사용자 알림 권한 요청
export const requestNotificationPermission = async () => {
  try {
    if (Platform.OS === 'ios') {
      const auth = await messaging().requestPermission();
      await setPermissionRequested();

      return (
        auth === messaging.AuthorizationStatus.AUTHORIZED ||
        auth === messaging.AuthorizationStatus.PROVISIONAL
      );
    }

    if (Platform.OS === 'android') {
      const { status } = await Notifications.requestPermissionsAsync();
      await setPermissionRequested();

      return status === 'granted';
    }

    return false;
  } catch (error) {
    console.error('권한 요청 실패:', error);
    return false;
  }
};

export const checkNotificationPermission = async () => {
  if (!Device.isDevice) {
    return false;
  }

  const authStatus = await messaging().hasPermission();
  return (
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL
  );
};

export const goToSystemPermission = async (enabled: boolean) => {
  try {
    const hasPermission = await checkNotificationPermission();
    if (enabled && !hasPermission) {
      Alert.alert(
        '알림 권한 필요',
        '알림을 받으려면 시스템 설정에서 알림 권한을 허용해주세요.',
        [
          { text: '취소', style: 'cancel' },
          {
            text: '설정 열기',
            onPress: () => {
              Linking.openSettings();
            },
          },
        ],
      );
    }
  } catch (error) {
    console.error('알림 권한 확인 실패:', error);
  }
};

export const getFCMToken = async () => {
  try {
    const hasPermission = await checkNotificationPermission();
    if (!hasPermission) {
      throw new Error('푸시 알림 권한이 없습니다.');
    }

    const token = await messaging().getToken();
    return token;
  } catch (error) {
    console.error('FCM 토큰을 가져오는데 실패했습니다.', error);
  }
};
