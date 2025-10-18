import { APP_VERSION } from './app';
import * as Device from 'expo-device';

export const WEBVIEW_USER_AGENT = `bombom/${APP_VERSION} ${Device.brand} ${Device.deviceType}`;
