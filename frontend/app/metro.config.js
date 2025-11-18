const { getDefaultConfig } = require('expo/metro-config');
const path = require('path');
const { resolve } = require('metro-resolver');

const defaultConfig = getDefaultConfig(__dirname);

if (!defaultConfig.resolver) {
  throw new Error('Metro resolver 설정이 없습니다.');
}

const customResolveRequest = (context, moduleName, platform) => {
  if (moduleName === '@bombom/shared/env') {
    return {
      filePath: path.resolve(__dirname, 'constants/env.ts'),
      type: 'sourceFile',
    };
  }

  return resolve(context, moduleName, platform);
};

/** @type {import('metro-config').ConfigT} */
const config = {
  ...defaultConfig,
  resolver: {
    ...defaultConfig.resolver,
    resolveRequest: customResolveRequest,
  },
};

module.exports = config;
