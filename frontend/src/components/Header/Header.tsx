import MobileHeader from './MobileHeader';
import PCHeader from './PCHeader';
import { useActiveNav } from '@/hooks/useActiveNav';
import { useDevice } from '@/hooks/useDevice';
import type { Device } from '@/hooks/useDevice';

export type HeaderVariant = Device | 'none';

const Header = () => {
  const activeNav = useActiveNav();
  const deviceType = useDevice();

  if (deviceType === 'pc') {
    return <PCHeader activeNav={activeNav} />;
  }

  return <MobileHeader activeNav={activeNav} />;
};

export default Header;
