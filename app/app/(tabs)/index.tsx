import { WebView } from "react-native-webview";

export default function HomeScreen() {
  return (
    <WebView source={{ uri: "https://www.bombom.news/" }} style={{ flex: 1 }} />
  );
}
