import { useEffect, useRef, useState } from 'react';
import { useLocalStorageState } from './useLocalStorageState';
import { isWebView } from '@/utils/device';
import { downloadApp } from '@/utils/downloadApp';

const APP_INSTALL_DISMISSED_KEY = 'app-install-prompt-dismissed';
const DISMISS_DURATION = 7 * 24 * 60 * 60 * 1000; // 7일

export function useAppInstallPrompt() {
  const modalRef = useRef<HTMLDivElement>(null);
  const [dismissedUntil, setDismissedUntil] = useLocalStorageState<number>(
    APP_INSTALL_DISMISSED_KEY,
  );
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    if (isWebView()) return;
    if (dismissedUntil && Date.now() < dismissedUntil) return;

    setShowModal(true);
  }, [dismissedUntil]);

  const handleInstallClick = () => {
    downloadApp();
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
    modalRef,
  };
}
