interface EnvConfig {
  baseUrl: string;
  token?: string;
}

let envInstance: EnvConfig | null = null;

export const setEnv = (config: EnvConfig) => {
  envInstance = config;
};

export const getEnv = () => {
  if (!envInstance) {
    throw new Error(
      '환경변수가 설정되지 않았습니다. setEnv를 통해 환경변수를 먼저 설정해주세요,',
    );
  }
  return envInstance;
};
