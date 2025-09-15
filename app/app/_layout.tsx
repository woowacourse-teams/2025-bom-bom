import "react-native-reanimated";
import { SafeAreaView } from "react-native-safe-area-context";

import WebView from "react-native-webview";

const CHROME_USER_AGENT =
  "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/96.0.4664.116 Mobile/15E148 Safari/604.1";

export default function RootLayout() {
  return (
    <SafeAreaView style={{ flex: 1 }}>
      <WebView
        source={{ uri: "https://dev.bombom.news" }}
        userAgent={CHROME_USER_AGENT}
        allowsBackForwardNavigationGestures
        sharedCookiesEnabled
        thirdPartyCookiesEnabled
        webviewDebuggingEnabled
        pullToRefreshEnabled
      />
    </SafeAreaView>
  );
}
