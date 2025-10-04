import { useWebView } from '@/contexts/WebViewContext';
import { useEffect } from 'react';
import { Alert, BackHandler, Platform } from 'react-native';

interface UseAndroidBackHandlerProps {
  canGoBack: boolean;
}

export const useAndroidBackHandler = ({
  canGoBack,
}: UseAndroidBackHandlerProps) => {
  const { sendMessageToWeb } = useWebView();

  useEffect(() => {
    if (Platform.OS !== 'android') return;

    const onBackPress = () => {
      if (canGoBack) {
        sendMessageToWeb({
          type: 'ANDROID_BACK_BUTTON_CLICKED',
        });
        return true;
      } else {
        Alert.alert('앱 종료', '앱을 종료하시겠습니까?', [
          { text: '취소', style: 'cancel' },
          {
            text: '종료',
            style: 'destructive',
            onPress: () => BackHandler.exitApp(),
          },
        ]);
      }

      return true;
    };

    const backHandler = BackHandler.addEventListener(
      'hardwareBackPress',
      onBackPress,
    );

    return () => backHandler.remove();
  }, [canGoBack, sendMessageToWeb]);
};
