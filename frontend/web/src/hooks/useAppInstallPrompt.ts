import { useEffect, useState } from 'react';
import { useDevice } from './useDevice';
import { useLocalStorageState } from './useLocalStorageState';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

const APP_INSTALL_DISMISSED_KEY = 'app-install-prompt-dismissed';

export function useAppInstallPrompt() {
  const device = useDevice();
  const [deferredPrompt, setDeferredPrompt] =
    useState<BeforeInstallPromptEvent | null>(null);
  const [dismissedUntil, setDismissedUntil] = useLocalStorageState<
    number | null
  >(APP_INSTALL_DISMISSED_KEY, null);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    if (device !== 'mobile') {
      return;
    }

    if (dismissedUntil && Date.now() < dismissedUntil) {
      return;
    }

    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      const event = e as BeforeInstallPromptEvent;
      setDeferredPrompt(event);
      setShowModal(true);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

    const isStandalone = window.matchMedia(
      '(display-mode: standalone)',
    ).matches;
    const isInApp =
      (window.navigator as any).standalone === true || isStandalone;

    if (!isInApp && device === 'mobile') {
      setShowModal(true);
    }

    return () => {
      window.removeEventListener(
        'beforeinstallprompt',
        handleBeforeInstallPrompt,
      );
    };
  }, [device, dismissedUntil]);

  const handleInstallClick = async () => {
    if (!deferredPrompt) {
      setShowModal(false);
      return;
    }

    await deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;

    if (outcome === 'accepted') {
      console.log('User accepted the install prompt');
    } else {
      console.log('User dismissed the install prompt');
    }

    setDeferredPrompt(null);
    setShowModal(false);
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
