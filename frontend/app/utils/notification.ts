import messaging from '@react-native-firebase/messaging';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';

// 안드로이드 알림 채널 생성
export const createAndroidChannel = async () => {
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: '기본 알림',
      importance: Notifications.AndroidImportance.DEFAULT,
    });
  }
};

// 사용자 알림 권한 요청
export const requestNotificationPermission = async () => {
  if (Device.isDevice) {
    const authStatus = await messaging().requestPermission();
    const enabled =
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL;

    return enabled;
  }

  return false;
};

// FCM 토큰 생성
export const getFCMToken = async () => {
  try {
    const hasPermission = await requestNotificationPermission();
    if (!hasPermission) {
      throw new Error('푸시 알림 권한이 없습니다.');
    }

    const token = await messaging().getToken();
    console.log(token);
    return token;
  } catch (error) {
    console.error('FCM 토큰을 가져오는데 실패했습니다.', error);
  }
};
