import React from 'react';
import { Pressable, Text } from 'react-native';
import styled from '@emotion/native';
import { useTheme } from '@emotion/react';

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
}

const StyledButton = styled(Pressable)<{
  variant: 'primary' | 'secondary' | 'outline';
  size: 'sm' | 'md' | 'lg';
  disabled: boolean;
}>`
  background-color: ${({ theme, variant, disabled }) => {
    if (disabled) return theme.colors.gray300;

    switch (variant) {
      case 'primary':
        return theme.colors.primary;
      case 'secondary':
        return theme.colors.gray200;
      case 'outline':
        return 'transparent';
      default:
        return theme.colors.primary;
    }
  }};

  border-width: ${({ variant }) => variant === 'outline' ? 1 : 0}px;
  border-color: ${({ theme, variant, disabled }) => {
    if (variant === 'outline') {
      return disabled ? theme.colors.gray300 : theme.colors.primary;
    }
    return 'transparent';
  }};

  border-radius: ${({ theme }) => theme.borderRadius.md}px;

  padding-vertical: ${({ theme, size }) => {
    switch (size) {
      case 'sm':
        return theme.spacing.sm;
      case 'md':
        return theme.spacing.md;
      case 'lg':
        return theme.spacing.lg;
      default:
        return theme.spacing.md;
    }
  }}px;

  padding-horizontal: ${({ theme, size }) => {
    switch (size) {
      case 'sm':
        return theme.spacing.md;
      case 'md':
        return theme.spacing.lg;
      case 'lg':
        return theme.spacing.xl;
      default:
        return theme.spacing.lg;
    }
  }}px;

  align-items: center;
  justify-content: center;

  opacity: ${({ disabled }) => disabled ? 0.6 : 1};
`;

const StyledText = styled(Text)<{
  variant: 'primary' | 'secondary' | 'outline';
  size: 'sm' | 'md' | 'lg';
  disabled: boolean;
}>`
  color: ${({ theme, variant, disabled }) => {
    if (disabled) return theme.colors.gray500;

    switch (variant) {
      case 'primary':
        return theme.colors.white;
      case 'secondary':
        return theme.colors.gray700;
      case 'outline':
        return theme.colors.primary;
      default:
        return theme.colors.white;
    }
  }};

  font-size: ${({ theme, size }) => {
    switch (size) {
      case 'sm':
        return theme.typography.fontSize.sm;
      case 'md':
        return theme.typography.fontSize.md;
      case 'lg':
        return theme.typography.fontSize.lg;
      default:
        return theme.typography.fontSize.md;
    }
  }}px;

  font-weight: 600;
  line-height: ${({ theme }) => theme.typography.lineHeight.normal};
`;

export const Button: React.FC<ButtonProps> = ({
  title,
  onPress,
  variant = 'primary',
  size = 'md',
  disabled = false,
}) => {
  const theme = useTheme();

  return (
    <StyledButton
      variant={variant}
      size={size}
      disabled={disabled}
      onPress={onPress}
      android_ripple={{
        color: variant === 'outline' ? theme.colors.primary : theme.colors.white,
        radius: 200,
      }}
    >
      <StyledText variant={variant} size={size} disabled={disabled}>
        {title}
      </StyledText>
    </StyledButton>
  );
};