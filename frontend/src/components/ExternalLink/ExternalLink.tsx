import { Link } from '@tanstack/react-router';
import React from 'react';
import { sendMessageToRN, isWebView } from '@/libs/webview/webview.utils';
import type { ReactNode } from 'react';

interface ExternalLinkProps {
  to: string;
  children: ReactNode;
  className?: string;
}

export const ExternalLink = ({
  to,
  children,
  className,
}: ExternalLinkProps) => {
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (!isWebView()) return;

    e.preventDefault();
    sendMessageToRN({
      type: 'OPEN_BROWSER',
      payload: { url: to },
    });
  };

  return (
    <Link to={to} onClick={handleClick} className={className}>
      {children}
    </Link>
  );
};
