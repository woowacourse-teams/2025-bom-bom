import styled from '@emotion/native';
import { Ionicons } from '@expo/vector-icons';
import React from 'react';

interface LoadingSpinnerProps {
  message?: string;
  iconName?: keyof typeof Ionicons.glyphMap;
  iconSize?: number;
  iconColor?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  message = '봄봄을 불러오는 중...',
  iconName = 'sparkles',
  iconSize = 32,
  iconColor = '#FE5E04',
}) => {
  return (
    <Container>
      <IconContainer>
        <Ionicons name={iconName} size={iconSize} color={iconColor} />
      </IconContainer>
      <LoadingText>{message}</LoadingText>
    </Container>
  );
};

const Container = styled.View`
  flex: 1;
  justify-content: center;
  align-items: center;
  background-color: #ffffff;
`;

const IconContainer = styled.View`
  margin-bottom: 16px;
`;

const LoadingText = styled.Text`
  font-size: 16px;
  color: #8e8e93;
  font-weight: 500;
`;
