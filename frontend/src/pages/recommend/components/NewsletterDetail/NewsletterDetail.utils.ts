import { MAILY_DOMAIN, STIBEE_DOMAIN } from './NewsletterDetail.constant';

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
