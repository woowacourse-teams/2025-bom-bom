import MobileHeader from './MobileHeader';
import PCHeader from './PCHeader';
import { useActiveNav } from '@/hooks/useActiveNav';
import { useDeviceType } from '@/hooks/useDeviceType';

const Header = () => {
  const activeNav = useActiveNav();
  const deviceType = useDeviceType();

  if (deviceType === 'pc') {
    return <PCHeader activeNav={activeNav} />;
  }

  return <MobileHeader activeNav={activeNav} />;
};

export default Header;
