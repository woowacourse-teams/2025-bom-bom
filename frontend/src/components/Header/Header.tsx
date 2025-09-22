import MobileHeader from './MobileHeader';
import PCHeader from './PCHeader';
import { useActiveNav } from '@/hooks/useActiveNav';
import { Device } from '@/hooks/useDeviceType';

export type HeaderVariant = Device | 'none';

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
