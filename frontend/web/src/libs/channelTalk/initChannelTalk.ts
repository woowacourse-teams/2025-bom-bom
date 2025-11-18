import { logger } from '@bombom/shared/utils';
import { ENV } from '@/apis/env';
import type { IChannelIO } from './channelTalk.types';

const CHANNEL_TALK_SCRIPT_SOURCE =
  'https://cdn.channel.io/plugin/ch-plugin-web.js';

const loadScript = () => {
  if (window.ChannelIO) {
    return logger.error('ChannelIO script included twice.');
  }

  const ch: IChannelIO = function (...args) {
    ch.c?.(args);
  };
  ch.q = [];
  ch.c = function (args) {
    ch.q?.push(args);
  };
  window.ChannelIO = ch;
};

export const initChannelTalk = () => {
  if (window.ChannelIOInitialized) return;

  loadScript();

  window.ChannelIOInitialized = true;
  const script = document.createElement('script');
  script.type = 'text/javascript';
  script.async = true;
  script.src = CHANNEL_TALK_SCRIPT_SOURCE;

  script.onload = () => {
    window.ChannelIO?.('boot', { pluginKey: ENV.channelTalkPluginKey });
  };

  const firstScript = document.getElementsByTagName('script')[0];
  if (firstScript?.parentNode) {
    firstScript.parentNode.insertBefore(script, firstScript);
  }
};
