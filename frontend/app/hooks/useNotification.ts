import { useEffect, useCallback } from 'react';
import * as Notifications from 'expo-notifications';
import messaging from '@react-native-firebase/messaging';
import { createAndroidChannel, getFCMToken } from '@/utils/notification';
import { useWebView } from '@/contexts/WebViewContext';
import { getDeviceUUID } from '@/utils/device';
import { postFCMToken } from '@/apis/notification';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

const useNotification = () => {
  const { sendMessageToWeb, isWebViewReady } = useWebView();

  const registerFCMToken = useCallback(async (memberId: number | undefined) => {
    try {
      const deviceUuid = await getDeviceUUID();
      const token = await getFCMToken();

      if (memberId && token && deviceUuid) {
        await postFCMToken({
          memberId,
          deviceUuid,
          token,
        });
      }
    } catch (error) {
      console.error('FCM 토큰 등록에 실패했습니다.', error);
    }
  }, []);

  // 앱 종료 상태에서 알림을 탭한 경우
  const coldStartNotificationOpen = useCallback(async () => {
    try {
      const message = await messaging().getInitialNotification();
      if (message && message.data?.notificationType === 'ARTICLE') {
        setTimeout(() => {
          sendMessageToWeb({
            type: 'NOTIFICATION_ROUTING',
            payload: { url: `/articles/${message.data?.articleId}` },
          });
        }, 800);
      }
    } catch (error) {
      console.error('앱 종료 상태의 알림 수신에 문제가 발생했습니다.', error);
    }
  }, [sendMessageToWeb]);

  useEffect(() => {
    createAndroidChannel();
  }, []);

  // WebView가 준비된 후에만 cold start 알림 처리
  useEffect(() => {
    if (!isWebViewReady) return;

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
      // ToDo: 백엔드에 새 토큰 전송
    });

    // 백그라운드에서 알림을 탭한 경우
    const unsubscribeNotificationOpened = messaging().onNotificationOpenedApp(
      (remoteMessage) => {
        if (remoteMessage.data?.notificationType === 'ARTICLE') {
          sendMessageToWeb({
            type: 'NOTIFICATION_ROUTING',
            payload: { url: `/articles/${remoteMessage.data?.articleId}` },
          });
        }
      },
    );

    // 포그라운드에서 알림을 탭한 경우
    const responseListener =
      Notifications.addNotificationResponseReceivedListener((response) => {
        const { data } = response.notification.request.content;

        if (data.notificationType === 'ARTICLE') {
          sendMessageToWeb({
            type: 'NOTIFICATION_ROUTING',
            payload: {
              url: `/articles/${data.articleId}`,
            },
          });
        }
      });

    // 클린업
    return () => {
      unsubscribeTokenRefresh();
      unsubscribe();
      unsubscribeNotificationOpened();
      responseListener.remove();
    };
  }, [coldStartNotificationOpen, isWebViewReady]);

  return {
    registerFCMToken,
  };
};

export default useNotification;
