import { useEffect } from 'react';
import MobileHeader from './MobileHeader';
import PCHeader from './PCHeader';
import { useActiveNav } from '@/hooks/useActiveNav';
import { DeviceType } from '@/hooks/useDeviceType';
import { useChannelTalk } from '@/libs/channelTalk/useChannelTalk';

export type HeaderVariant = DeviceType | 'none';

interface HeaderProps {
  variant: HeaderVariant;
}

const Header = ({ variant }: HeaderProps) => {
  const activeNav = useActiveNav();
  const { showMessenger, showChannelButton, hideChannelButton } =
    useChannelTalk();

  useEffect(() => {
    if (variant === 'pc') {
      showChannelButton();
    } else {
      hideChannelButton();
    }
  }, [hideChannelButton, showChannelButton, variant]);

  if (variant === 'none') return;

  if (variant === 'pc') {
    return <PCHeader activeNav={activeNav} />;
  }

  return <MobileHeader activeNav={activeNav} showMessenger={showMessenger} />;
};

export default Header;
