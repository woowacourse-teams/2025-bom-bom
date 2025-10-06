import styled from '@emotion/native';
import React, { ReactNode } from 'react';
import { ActivityIndicator, TextStyle, ViewStyle } from 'react-native';

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'social';
  disabled?: boolean;
  loading?: boolean;
  style?: ViewStyle;
  textStyle?: TextStyle;
  icon?: ReactNode;
}

export const Button = ({
  title,
  onPress,
  variant = 'primary',
  disabled = false,
  loading = false,
  style,
  textStyle,
  icon,
}: ButtonProps) => {
  const StyledButton =
    variant === 'primary'
      ? PrimaryButton
      : variant === 'secondary'
        ? SecondaryButton
        : SocialButton;

  const StyledText =
    variant === 'primary'
      ? PrimaryText
      : variant === 'secondary'
        ? SecondaryText
        : SocialText;

  return (
    <StyledButton
      style={style}
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.8}
      $disabled={disabled}
    >
      {loading ? (
        <ActivityIndicator
          size="small"
          color={variant === 'primary' ? '#FFFFFF' : '#FE5E04'}
        />
      ) : (
        <>
          {icon}
          <StyledText style={textStyle}>{title}</StyledText>
        </>
      )}
    </StyledButton>
  );
};

const BaseButton = styled.TouchableOpacity<{ $disabled?: boolean }>`
  flex-direction: row;
  align-items: center;
  justify-content: center;
  padding: 12px 24px;
  border-radius: 8px;
  min-height: 48px;
  gap: 12px;
  opacity: ${(props) => (props.$disabled ? 0.5 : 1)};
`;

const PrimaryButton = styled(BaseButton)`
  background-color: ${(props) => props.theme.colors.primary};
`;

const SecondaryButton = styled(BaseButton)`
  background-color: transparent;
  border-width: 2px;
  border-color: ${(props) => props.theme.colors.primary};
`;

const SocialButton = styled(BaseButton)`
  background-color: ${(props) => props.theme.colors.white};
  border-width: 2px;
  border-color: ${(props) => props.theme.colors.stroke};
  shadow-color: ${(props) => props.theme.colors.black};
  shadow-offset: 0px 2px;
  shadow-opacity: 0.1;
  shadow-radius: 3px;
  elevation: 3;
`;

const BaseText = styled.Text`
  font-size: 16px;
  font-weight: 600;
  text-align: center;
`;

const PrimaryText = styled(BaseText)`
  color: ${(props) => props.theme.colors.white};
`;

const SecondaryText = styled(BaseText)`
  color: ${(props) => props.theme.colors.primary};
`;

const SocialText = styled(BaseText)`
  color: ${(props) => props.theme.colors.black};
`;
