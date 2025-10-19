import { useState, useEffect, useCallback } from 'react';
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

  // 안드로이드 알림 채널 생성
  const createAndroidChannel = useCallback(async () => {
    if (Platform.OS === 'android') {
      await Notifications.setNotificationChannelAsync('default', {
        name: '기본 알림',
        importance: Notifications.AndroidImportance.DEFAULT,
      });
    }
  }, []);

  // 사용자 알림 권한 요청
  const requestNotificationPermission = useCallback(async () => {
    if (Device.isDevice) {
      const authStatus = await messaging().requestPermission();
      const enabled =
        authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
        authStatus === messaging.AuthorizationStatus.PROVISIONAL;

      return enabled;
    }

    return false;
  }, []);

  const getFcmToken = useCallback(async () => {
    try {
      const hasPermission = await requestNotificationPermission();
      if (!hasPermission) {
        console.error('푸시 알림 권한이 없습니다.');
      }

      const token = await messaging().getToken();
      setFcmToken(token);
    } catch (error) {
      console.error('FCM 토큰을 가져오는데 실패했습니다.', error);
    }
  }, [requestNotificationPermission]);

  useEffect(() => {
    createAndroidChannel();
    getFcmToken();

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

    // 앱 종료 상태에서 알림을 탭한 경우
    messaging()
      .getInitialNotification()
      .then((remoteMessage) => {
        if (remoteMessage) {
          // ToDo: 특정 화면으로 이동
        }
      });

    // 백그라운드 상태에서 알림을 탭한 경우
    const unsubscribeNotificationOpened = messaging().onNotificationOpenedApp(
      (remoteMessage) => {
        // ToDo: 특정 화면으로 이동
      },
    );

    // 알림 수신 리스너: 알림이 표시되면 state에 저장
    const notificationListener = Notifications.addNotificationReceivedListener(
      (notification) => {
        setNotification(notification);
      },
    );

    // 알림 탭 리스너: 사용자가 알림을 탭했을 때 동작을 처리
    const responseListener =
      Notifications.addNotificationResponseReceivedListener((response) => {});

    // 클린업
    return () => {
      unsubscribeTokenRefresh(); // FCM 토큰 갱신 리스너 제거
      unsubscribe(); // FCM 포그라운드 메시지 리스너 제거
      unsubscribeNotificationOpened(); // 백그라운드 알림 탭 리스너 제거
      notificationListener.remove(); // 알림 수신 리스너 제거
      responseListener.remove(); // 알림 탭 리스너 제거
    };
  }, [createAndroidChannel, getFcmToken]);

  return {
    fcmToken,
    notification,
  };
};

export default useNotification;
