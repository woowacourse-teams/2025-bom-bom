import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';

import { isWebView } from '@/libs/webview/webview.utils';
import { openExternalLink } from '@/utils/externalLink';
import type { ReactNode } from 'react';

interface ExternalLinkProps {
  to: string;
  children: ReactNode;
  underline?: boolean;
}

export const ExternalLink = ({
  to,
  children,
  underline = true,
}: ExternalLinkProps) => {
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (!isWebView()) return;

    e.preventDefault();
    openExternalLink(to);
  };

  return (
    <StyledLink to={to} onClick={handleClick} underline={underline}>
      {children}
    </StyledLink>
  );
};

export const StyledLink = styled(Link)<{ underline: boolean }>`
  text-decoration: ${({ underline }) => (underline ? 'underline' : 'none')};
`;
