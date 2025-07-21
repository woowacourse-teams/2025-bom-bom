module.exports = {
  root: true,
  env: { browser: true, es2020: true, 'jest/globals': true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
    'plugin:jsx-a11y/recommended',
    'plugin:import/recommended',
    'plugin:react/recommended',
    'plugin:prettier/recommended',
    'plugin:storybook/recommended',
  ],
  ignorePatterns: ['dist', '.eslintrc.cjs'],
  rules: {
    'react/react-in-jsx-scope': 'off',
    'import/no-unresolved': 'off',
  },
  plugins: ['jsx-a11y', 'import', 'react-hooks', 'react', 'jest', 'prettier'],
  parser: '@typescript-eslint/parser',
  settings: {
    react: {
      version: 'detect',
    },
  },
};
