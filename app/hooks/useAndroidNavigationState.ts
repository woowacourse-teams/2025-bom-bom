import { useWebView } from '@/contexts/WebViewContext';
import { useCallback, useEffect, useState } from 'react';
import { Alert, BackHandler, Platform } from 'react-native';
import { WebViewNavigation } from 'react-native-webview';

const useAndroidNavigationState = () => {
  const [canGoBack, setCanGoBack] = useState(false);
  const { sendMessageToWeb } = useWebView();

  const handleNavigationStateChange = useCallback(
    (navState: WebViewNavigation) => {
      setCanGoBack(navState.canGoBack);
    },
    [],
  );

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
  }, [canGoBack, sendMessageToWeb, handleNavigationStateChange]);

  return {
    handleNavigationStateChange,
  };
};

export default useAndroidNavigationState;
