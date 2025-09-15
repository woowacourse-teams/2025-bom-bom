import MobileHeader from './MobileHeader';
import PCHeader from './PCHeader';
import { useActiveNav } from '@/hooks/useActiveNav';
import { DeviceType } from '@/hooks/useDeviceType';

export type HeaderVariant = DeviceType | 'none';

interface HeaderProps {
  variant: HeaderVariant;
}

const Header = ({ variant }: HeaderProps) => {
  const activeNav = useActiveNav();

  if (variant === 'none') return;

  if (variant === 'pc') {
    return <PCHeader activeNav={activeNav} />;
  }

  return <MobileHeader activeNav={activeNav} />;
};

export default Header;
