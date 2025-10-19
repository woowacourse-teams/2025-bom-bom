import { useState, useEffect } from 'react';
import { Platform } from 'react-native';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import messaging from '@react-native-firebase/messaging';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldPlaySound: false,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

const useNotification = () => {
  const [fcmToken, setFcmToken] = useState('');
  const [notification, setNotification] =
    useState<Notifications.Notification | null>(null);

  useEffect(() => {
    // 앱 실행 시 FCM 토큰을 가져와서 state에 저장
    registerForPushNotificationsAsync().then(
      (token) => token && setFcmToken(token),
    );

    // FCM 포그라운드 메시지 리스너: 앱이 열려있을 때 FCM 메시지를 받으면 즉시 로컬 알림으로 표시
    const unsubscribe = messaging().onMessage(async (remoteMessage) => {
      // FCM에서 메시지를 받으면 Expo Notifications로 로컬 알림 표시
      if (remoteMessage.notification) {
        await Notifications.scheduleNotificationAsync({
          content: {
            title: remoteMessage.notification.title || '',
            body: remoteMessage.notification.body || '',
            data: remoteMessage.data,
          },
          trigger: null, // 즉시 표시
        });
      }
    });

    // 알림 수신 리스너: 알림이 표시되면 state에 저장
    const notificationListener = Notifications.addNotificationReceivedListener(
      (notification) => {
        setNotification(notification);
      },
    );

    // 알림 탭 리스너: 사용자가 알림을 탭했을 때 동작을 처리
    const responseListener =
      Notifications.addNotificationResponseReceivedListener((response) => {
        console.log('Notification tapped:', response);
      });

    // 클린업
    return () => {
      unsubscribe(); // FCM 포그라운드 메시지 리스너 제거
      notificationListener.remove(); // 알림 수신 리스너 제거
      responseListener.remove(); // 알림 탭 리스너 제거
    };
  }, []);

  return {
    fcmToken,
    notification,
  };
};

async function registerForPushNotificationsAsync() {
  let token;

  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('myNotificationChannel', {
      name: 'A channel is needed for the permissions prompt to appear',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#FF231F7C',
    });
  }

  if (Device.isDevice) {
    // FCM 권한 요청
    const authStatus = await messaging().requestPermission();
    const enabled =
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL;

    if (!enabled) {
      alert('Failed to get push notification permission!');
      return;
    }

    try {
      // FCM 토큰 가져오기
      token = await messaging().getToken();
      console.log('FCM Token:', token);
    } catch (e) {
      console.error('Error getting FCM token:', e);
      token = `${e}`;
    }
  } else {
    alert('Must use physical device for Push Notifications');
  }

  return token;
}

export default useNotification;
