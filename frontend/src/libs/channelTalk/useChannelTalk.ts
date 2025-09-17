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
        { pluginKey: PLUGIN_KEY, ...bootOption },
        bootCallback,
      );
    };

    const firstScript = document.getElementsByTagName('script')[0];
    if (firstScript?.parentNode) {
      firstScript.parentNode.insertBefore(script, firstScript);
    }
  }, [bootOption, bootCallback, loadScript]);

  /* eslint-disable @typescript-eslint/no-explicit-any */
  const callChannelIO = useCallback((method: string, ...args: any[]) => {
    window.ChannelIO?.(method, ...args);
  }, []);

  const shutdown = useCallback(
    () => callChannelIO('shutdown'),
    [callChannelIO],
  );
  const showMessenger = useCallback(
    () => callChannelIO('showMessenger'),
    [callChannelIO],
  );
  const hideMessenger = useCallback(
    () => callChannelIO('hideMessenger'),
    [callChannelIO],
  );
  const clearCallbacks = useCallback(
    () => callChannelIO('clearCallbacks'),
    [callChannelIO],
  );
  const resetPage = useCallback(
    () => callChannelIO('resetPage'),
    [callChannelIO],
  );
  const showChannelButton = useCallback(
    () => callChannelIO('showChannelButton'),
    [callChannelIO],
  );
  const hideChannelButton = useCallback(
    () => callChannelIO('hideChannelButton'),
    [callChannelIO],
  );

  const openChat = useCallback(
    (chatId?: string | number, message?: string) =>
      callChannelIO('openChat', chatId, message),
    [callChannelIO],
  );

  const track = useCallback(
    (eventName: string, eventProperty?: EventProperty) =>
      callChannelIO('track', eventName, eventProperty),
    [callChannelIO],
  );

  const updateUser = useCallback(
    (userInfo: UpdateUserInfo, callback?: Callback) =>
      callChannelIO('updateUser', userInfo, callback),
    [callChannelIO],
  );

  const addTags = useCallback(
    (tags: string[], callback?: Callback) =>
      callChannelIO('addTags', tags, callback),
    [callChannelIO],
  );

  const removeTags = useCallback(
    (tags: string[], callback?: Callback) =>
      callChannelIO('removeTags', tags, callback),
    [callChannelIO],
  );

  const setPage = useCallback(
    (page: string) => callChannelIO('setPage', page),
    [callChannelIO],
  );

  const setAppearance = useCallback(
    (appearance: Appearance) => callChannelIO('setAppearance', appearance),
    [callChannelIO],
  );

  const onShowMessenger = useCallback(
    (callback: () => void) => callChannelIO('onShowMessenger', callback),
    [callChannelIO],
  );

  const onHideMessenger = useCallback(
    (callback: () => void) => callChannelIO('onHideMessenger', callback),
    [callChannelIO],
  );

  const onBadgeChanged = useCallback(
    (callback: (unread: number, alert: number) => void) =>
      callChannelIO('onBadgeChanged', callback),
    [callChannelIO],
  );

  const onChatCreated = useCallback(
    (callback: () => void) => callChannelIO('onChatCreated', callback),
    [callChannelIO],
  );

  const onFollowUpChanged = useCallback(
    (callback: (profile: FollowUpProfile) => void) =>
      callChannelIO('onFollowUpChanged', callback),
    [callChannelIO],
  );

  const onUrlClicked = useCallback(
    (callback: (url: string) => void) =>
      callChannelIO('onUrlClicked', callback),
    [callChannelIO],
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
