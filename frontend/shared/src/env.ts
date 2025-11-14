export const ENV = {
  baseUrl: (() => {
    throw new Error(
      'ENV가 설정되지 않았습니다. webpack 또는 metro.config 설정을 확인해주세요.',
    );
  })(),
} as const;
