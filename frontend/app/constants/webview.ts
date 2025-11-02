import * as Device from 'expo-device';
import { version } from '../package.json';
import { Platform } from 'react-native';

export const WEBVIEW_USER_AGENT = `bombom/${version} ${Device.brand} ${Platform.OS}`;
