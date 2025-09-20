import { useEffect, useCallback } from 'react';
import { BootOption, Callback, IChannelIO } from './channelTalk.types';
import { ENV } from '@/apis/env';

interface UseChannelTalkParams {
  bootOption?: BootOption;
  bootCallback?: Callback;
}

export const useChannelTalk = ({
  bootOption,
  bootCallback,
}: UseChannelTalkParams = {}) => {
  const loadScript = useCallback(() => {
    if (window.ChannelIO) {
      return console.error('ChannelIO script included twice.');
    }

    const ch: IChannelIO = function (...args) {
      ch.c?.(args);
    };
    ch.q = [];
    ch.c = function (args) {
      ch.q?.push(args);
    };
    window.ChannelIO = ch;
  }, []);

  const initChannelTalk = useCallback(() => {
    if (window.ChannelIOInitialized) return;

    loadScript();

    window.ChannelIOInitialized = true;
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.async = true;
    script.src = 'https://cdn.channel.io/plugin/ch-plugin-web.js';

    script.onload = () => {
      window.ChannelIO?.(
        'boot',
        { pluginKey: ENV.pluginKey, ...bootOption },
        bootCallback,
      );
    };

    const firstScript = document.getElementsByTagName('script')[0];
    if (firstScript?.parentNode) {
      firstScript.parentNode.insertBefore(script, firstScript);
    }
  }, [bootOption, bootCallback, loadScript]);

  const showMessenger = useCallback(
    () => window.ChannelIO?.('showMessenger'),
    [],
  );

  useEffect(() => {
    if (document.readyState === 'complete') {
      initChannelTalk();
      return;
    }

    window.addEventListener('DOMContentLoaded', initChannelTalk);
    window.addEventListener('load', initChannelTalk);

    return () => {
      window.removeEventListener('DOMContentLoaded', initChannelTalk);
      window.removeEventListener('load', initChannelTalk);
    };
  }, [initChannelTalk]);

  return {
    showMessenger,
  };
};
