module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
    'plugin:jsx-a11y/recommended',
    'plugin:n/recommended',
    'plugin:promise/recommended',
    'plugin:import/recommended',
    'plugin:react/recommended',
  ],
  ignorePatterns: ['dist', '.eslintrc.js'],
  rules: {
    'react/react-in-jsx-scope': 'off',
  },
  plugins: ['jsx-a11y', 'n', 'promise', 'import', 'react-hooks', 'react'],
  parser: '@typescript-eslint/parser',
};
