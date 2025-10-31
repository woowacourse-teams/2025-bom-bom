import messaging from '@react-native-firebase/messaging';
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

// 사용자 알림 권한 요청
export const requestNotificationPermission = async () => {
  if (!Device.isDevice) {
    return false;
  }

  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;

  return enabled;
};

export const checkNotificationPermission = async () => {
  if (Device.isDevice) {
    const authStatus = await messaging().hasPermission();
    return (
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL
    );
  }
  return false;
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

// FCM 토큰 생성
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
