import type { StorybookConfig } from '@storybook/react-webpack5';
import { TsconfigPathsPlugin } from 'tsconfig-paths-webpack-plugin';
import webpack from 'webpack';

const config: StorybookConfig = {
  stories: ['../src/**/*.mdx', '../src/**/*.stories.@(js|jsx|mjs|ts|tsx)'],
  addons: ['@storybook/addon-webpack5-compiler-swc', '@storybook/addon-docs'],
  framework: {
    name: '@storybook/react-webpack5',
    options: {},
  },
  webpackFinal: async (config) => {
    config.plugins?.push(
      new webpack.EnvironmentPlugin({
        API_BASE_URL: 'https://api-dev.bombom.news/api/v1',
        API_TOKEN: '',
        ENABLE_MSW: 'false',
      }),
      new webpack.DefinePlugin({
        'process.env': JSON.stringify(process.env),
      }),
    );

    const imageRule = config.module?.rules?.find((rule) => {
      const test = (rule as { test: RegExp }).test;

      if (!test) {
        return false;
      }

      return test.test('.svg');
    }) as { [key: string]: any };

    imageRule.exclude = /\.svg$/;

    config.module?.rules?.push({
      test: /\.svg$/,
      use: ['@svgr/webpack'],
    });

    config.module?.rules?.push({
      test: /\.(avif|png|jpe?g|gif|svg)$/i,
      type: 'asset/resource',
      generator: {
        filename: 'static/media/[name].[hash][ext]',
      },
    });

    return {
      ...config,
      resolve: {
        ...config.resolve,
        plugins: [
          ...(config.resolve?.plugins || []),
          new TsconfigPathsPlugin({}),
        ],
      },
    };
  },
};
export default config;
