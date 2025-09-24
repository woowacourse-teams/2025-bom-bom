import { MAILY_URL, STIBEE_URL } from './NewsletterDetail.constant';

export const buildSubscribeUrl = (
  baseUrl: string,
  userInfo: { email: string; nickname: string },
) => {
  const url = new URL(baseUrl);

  if (baseUrl.includes(STIBEE_URL)) {
    url.searchParams.set('email', userInfo.email);
    url.searchParams.set('name', userInfo.nickname);
  } else if (baseUrl.includes(MAILY_URL)) {
    url.searchParams.set('email', userInfo.email);
    url.searchParams.set('nickname', userInfo.nickname);
    url.searchParams.set('pop', 'up');
  }

  return url.toString();
};
