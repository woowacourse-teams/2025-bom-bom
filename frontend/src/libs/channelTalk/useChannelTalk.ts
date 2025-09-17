import { useEffect, useCallback } from 'react';
import {
  Appearance,
  BootOption,
  Callback,
  EventProperty,
  FollowUpProfile,
  IChannelIO,
  UpdateUserInfo,
} from './channelTalk.types';
import { ENV } from '@/apis/env';

const PLUGIN_KEY = ENV.pluginKey;

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

  const loadChannelIOScript = useCallback(() => {
    if (window.ChannelIOInitialized) return;
    window.ChannelIOInitialized = true;

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.async = true;
    script.src = 'https://cdn.channel.io/plugin/ch-plugin-web.js';

    const firstScript = document.getElementsByTagName('script')[0];
    if (firstScript?.parentNode) {
      firstScript.parentNode.insertBefore(script, firstScript);
    }
  }, []);

  const boot = useCallback((option: BootOption, callback?: Callback) => {
    window.ChannelIO?.('boot', option, callback);
  }, []);

  const shutdown = useCallback(() => {
    window.ChannelIO?.('shutdown');
  }, []);

  const openChat = useCallback((chatId?: string | number, message?: string) => {
    window.ChannelIO?.('openChat', chatId, message);
  }, []);

  const showMessenger = useCallback(() => {
    window.ChannelIO?.('showMessenger');
  }, []);

  const hideMessenger = useCallback(() => {
    window.ChannelIO?.('hideMessenger');
  }, []);

  const onShowMessenger = useCallback((callback: () => void) => {
    window.ChannelIO?.('onShowMessenger', callback);
  }, []);

  const onHideMessenger = useCallback((callback: () => void) => {
    window.ChannelIO?.('onHideMessenger', callback);
  }, []);

  const track = useCallback(
    (eventName: string, eventProperty?: EventProperty) => {
      window.ChannelIO?.('track', eventName, eventProperty);
    },
    [],
  );

  const onBadgeChanged = useCallback(
    (callback: (unread: number, alert: number) => void) => {
      window.ChannelIO?.('onBadgeChanged', callback);
    },
    [],
  );

  const onChatCreated = useCallback((callback: () => void) => {
    window.ChannelIO?.('onChatCreated', callback);
  }, []);

  const onFollowUpChanged = useCallback(
    (callback: (profile: FollowUpProfile) => void) => {
      window.ChannelIO?.('onFollowUpChanged', callback);
    },
    [],
  );

  const onUrlClicked = useCallback((callback: (url: string) => void) => {
    window.ChannelIO?.('onUrlClicked', callback);
  }, []);

  const clearCallbacks = useCallback(() => {
    window.ChannelIO?.('clearCallbacks');
  }, []);

  const updateUser = useCallback(
    (userInfo: UpdateUserInfo, callback?: Callback) => {
      window.ChannelIO?.('updateUser', userInfo, callback);
    },
    [],
  );

  const addTags = useCallback((tags: string[], callback?: Callback) => {
    window.ChannelIO?.('addTags', tags, callback);
  }, []);

  const removeTags = useCallback((tags: string[], callback?: Callback) => {
    window.ChannelIO?.('removeTags', tags, callback);
  }, []);

  const setPage = useCallback((page: string) => {
    window.ChannelIO?.('setPage', page);
  }, []);

  const resetPage = useCallback(() => {
    window.ChannelIO?.('resetPage');
  }, []);

  const showChannelButton = useCallback(() => {
    window.ChannelIO?.('showChannelButton');
  }, []);

  const hideChannelButton = useCallback(() => {
    window.ChannelIO?.('hideChannelButton');
  }, []);

  const setAppearance = useCallback((appearance: Appearance) => {
    window.ChannelIO?.('setAppearance', appearance);
  }, []);

  useEffect(() => {
    if (document.readyState === 'complete') {
      loadChannelIOScript();
      return;
    }

    window.addEventListener('DOMContentLoaded', loadChannelIOScript);
    window.addEventListener('load', loadChannelIOScript);

    return () => {
      window.removeEventListener('DOMContentLoaded', loadChannelIOScript);
      window.removeEventListener('load', loadChannelIOScript);
    };
  }, [loadChannelIOScript]);

  useEffect(() => {
    loadScript();

    boot({ pluginKey: PLUGIN_KEY, ...bootOption }, bootCallback);
  }, []);

  return {
    boot,
    shutdown,
    showMessenger,
    hideMessenger,
    openChat,
    track,
    onShowMessenger,
    onHideMessenger,
    onBadgeChanged,
    onChatCreated,
    onFollowUpChanged,
    onUrlClicked,
    clearCallbacks,
    updateUser,
    addTags,
    removeTags,
    setPage,
    resetPage,
    showChannelButton,
    hideChannelButton,
    setAppearance,
  };
};
