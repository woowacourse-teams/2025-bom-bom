import { useEffect } from 'react';
import MobileHeader from './MobileHeader';
import PCHeader from './PCHeader';
import { useActiveNav } from '@/hooks/useActiveNav';
import { useDeviceType } from '@/hooks/useDeviceType';
import { useChannelTalk } from '@/libs/channelTalk/useChannelTalk';

const Header = () => {
  const activeNav = useActiveNav();
  const deviceType = useDeviceType();
  const { showChannelButton, hideChannelButton } = useChannelTalk();

  useEffect(() => {
    if (deviceType === 'pc') {
      showChannelButton();
    } else {
      hideChannelButton();
    }
  }, [hideChannelButton, showChannelButton, deviceType]);

  if (deviceType === 'pc') {
    return <PCHeader activeNav={activeNav} />;
  }

  return <MobileHeader activeNav={activeNav} />;
};

export default Header;
