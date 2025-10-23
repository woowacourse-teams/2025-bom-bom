import * as Device from 'expo-device';
import { version } from '../package.json';

export const WEBVIEW_USER_AGENT = `bombom/${version} ${Device.brand}`;
