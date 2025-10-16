import { MAILY_DOMAIN, STIBEE_DOMAIN } from './NewsletterDetail.constant';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { copyToClipboard } from '@/utils/copy';
import { openExternalLink } from '@/utils/externalLink';
import type { UserInfo } from '@/types/me';

export const isStibee = (baseUrl: string) => {
  return baseUrl.includes(STIBEE_DOMAIN);
};

export const isMaily = (baseUrl: string) => {
  return baseUrl.includes(MAILY_DOMAIN);
};

export const buildSubscribeUrl = (
  baseUrl: string,
  userInfo: { email: string; nickname: string },
) => {
  const url = new URL(baseUrl);

  if (isStibee(baseUrl)) {
    url.searchParams.set('email', userInfo.email);
    url.searchParams.set('name', userInfo.nickname);
  }
  if (isMaily(baseUrl)) {
    url.searchParams.set('email', userInfo.email);
    url.searchParams.set('nickname', userInfo.nickname);
    url.searchParams.set('pop', 'up');
  }

  return url.toString();
};

export const openSubscribeLink = (
  link: string,
  newsletterName: string,
  userInfo?: UserInfo | null,
) => {
  if (!userInfo) return;

  if (!isStibee(link) && !isMaily(link)) {
    copyToClipboard(userInfo.email);
    alert('이메일이 복사되었습니다. 이 이메일로 뉴스레터를 구독해주세요.');
  }

  trackEvent({
    category: 'Newsletter',
    action: '구독하기 버튼 클릭',
    label: newsletterName ?? 'Unknown Newsletter',
  });

  const subscribeUrl = buildSubscribeUrl(link, userInfo);

  openExternalLink(subscribeUrl);
};
