import { getDefaultConfig } from 'expo/metro-config';
import path from 'path';
import { resolve } from 'metro-resolver';
import type { ResolutionContext, Resolution } from 'metro-resolver';

const defaultConfig = getDefaultConfig(__dirname);

if (!defaultConfig.resolver) {
  throw new Error('Metro resolver 설정이 없습니다.');
}

const customResolveRequest = (
  context: ResolutionContext,
  moduleName: string,
  platform: string | null,
): Resolution => {
  if (moduleName === '@bombom/shared/env') {
    return {
      filePath: path.resolve(__dirname, 'constants/env.ts'),
      type: 'sourceFile',
    };
  }

  return resolve(context, moduleName, platform);
};

const config = {
  ...defaultConfig,
  resolver: {
    ...defaultConfig.resolver,
    resolveRequest: customResolveRequest,
  },
};

export default config;
