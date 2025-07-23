export const CATEGORIES = [
  '전체',
  '트렌드/라이프',
  '엔터테인먼트',
  '비즈/재테크',
  '지역/여행',
  '푸드',
  'IT/테크',
  '디자인',
  '시사/사회',
  '취미/자기개발',
  '문화/예술',
  '리빙/인테리어',
  '건강/의학',
] as const;

export type CategoryType = (typeof CATEGORIES)[number];
