import { useEffect, useState } from 'react';
import { useLocalStorageState } from './useLocalStorageState';
import { isWebView } from '@/libs/webview/webview.utils';
import { isMobileByUserAgent } from '@/utils/device';

const APP_INSTALL_DISMISSED_KEY = 'app-install-prompt-dismissed';
const DISMISS_DURATION = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds

export function useAppInstallPrompt() {
  const [dismissedUntil, setDismissedUntil] = useLocalStorageState<number>(
    APP_INSTALL_DISMISSED_KEY,
  );
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    if (!isMobileByUserAgent() || isWebView()) return;
    if (dismissedUntil && Date.now() < dismissedUntil) return;

    setShowModal(true);
  }, [dismissedUntil]);

  const handleInstallClick = async () => {
    // 앱 설치
  };

  const handleLaterClick = () => {
    setDismissedUntil(Date.now() + DISMISS_DURATION);
    setShowModal(false);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  return {
    showModal,
    handleInstallClick,
    handleLaterClick,
    handleCloseModal,
  };
}
