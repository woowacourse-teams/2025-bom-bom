import React from 'react';
import { sendMessageToRN, isWebView } from '@/libs/webview/webview.utils';
import type { ReactNode } from 'react';

interface ExternalLinkProps {
  href: string;
  children: ReactNode;
  className?: string;
}

export const ExternalLink = ({
  href,
  children,
  className,
}: ExternalLinkProps) => {
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (!isWebView()) return;

    e.preventDefault();
    sendMessageToRN({
      type: 'OPEN_BROWSER',
      payload: { url: href },
    });
  };

  return (
    <a href={href} onClick={handleClick} className={className}>
      {children}
    </a>
  );
};
