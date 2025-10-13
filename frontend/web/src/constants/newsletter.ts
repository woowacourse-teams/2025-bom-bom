export const CATEGORIES = [
  '전체',
  '트렌드/라이프',
  '비즈/재테크',
  '지역/여행',
  '푸드',
  'IT/테크',
  '시사/사회',
  '취미/자기개발',
  '문화/예술',
  '리빙/인테리어',
] as const;

export type Category = (typeof CATEGORIES)[number];

export const NEWSLETTER_COUNT = {
  nonMobile: 52,
  mobile: 5,
};
