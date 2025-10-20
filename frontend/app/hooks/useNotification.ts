import { useState, useEffect, useCallback } from 'react';
import * as Notifications from 'expo-notifications';
import messaging from '@react-native-firebase/messaging';
import {
  createAndroidChannel,
  requestNotificationPermission,
} from '@/utils/notification';

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

  const getFcmToken = useCallback(async () => {
    try {
      const hasPermission = await requestNotificationPermission();
      if (!hasPermission) {
        throw new Error('푸시 알림 권한이 없습니다.');
      }

      const token = await messaging().getToken();
      console.log(token);
      setFcmToken(token);
    } catch (error) {
      console.error('FCM 토큰을 가져오는데 실패했습니다.', error);
    }
  }, []);

  // 앱 종료 상태에서 알림을 탭한 경우
  const coldStartNotificationOpen = useCallback(async () => {
    try {
      const message = await messaging().getInitialNotification();
      if (message) {
        // ToDo: 특정 화면으로 이동
      }
    } catch (error) {
      console.error('앱 종료 상태의 알림 수신에 문제가 발생했습니다.', error);
    }
  }, []);

  useEffect(() => {
    createAndroidChannel();
    getFcmToken();
    coldStartNotificationOpen();

    // FCM 포그라운드 메시지 리스너: 앱이 열려있을 때 FCM 메시지를 받으면 즉시 로컬 알림으로 표시
    const unsubscribe = messaging().onMessage(async (remoteMessage) => {
      // FCM에서 메시지를 받으면 Expo Notifications로 로컬 알림 표시
      if (remoteMessage.notification) {
        await Notifications.scheduleNotificationAsync({
          content: {
            title: remoteMessage.notification.title,
            body: remoteMessage.notification.body,
            data: remoteMessage.data,
          },
          trigger: null, // 즉시 표시 (타이머 없음)
        });
      }
    });

    // FCM 토큰 갱신 리스너
    const unsubscribeTokenRefresh = messaging().onTokenRefresh((newToken) => {
      setFcmToken(newToken);
      // ToDo: 백엔드에 새 토큰 전송
    });

    // 백그라운드에서 알림을 탭한 경우
    const unsubscribeNotificationOpened = messaging().onNotificationOpenedApp(
      (remoteMessage) => {
        // ToDo: 특정 화면으로 이동
      },
    );

    // 포그라운드에서 알림을 수신한 경우
    const notificationListener = Notifications.addNotificationReceivedListener(
      (notification) => {
        setNotification(notification);
      },
    );

    // 포그라운드에서 알림을 탭한 경우
    const responseListener =
      Notifications.addNotificationResponseReceivedListener((response) => {});

    // 클린업
    return () => {
      unsubscribeTokenRefresh();
      unsubscribe();
      unsubscribeNotificationOpened();
      notificationListener.remove();
      responseListener.remove();
    };
  }, [coldStartNotificationOpen, getFcmToken]);

  return {
    fcmToken,
    notification,
  };
};

export default useNotification;
