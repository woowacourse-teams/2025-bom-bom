import 'react-native-reanimated';
import { SafeAreaView } from 'react-native-safe-area-context';

import WebView from 'react-native-webview';

export default function RootLayout() {
  return (
    <SafeAreaView style={{ flex: 1 }}>
      <WebView source={{ uri: 'https://dev.bombom.news' }} />
    </SafeAreaView>
  );
}
