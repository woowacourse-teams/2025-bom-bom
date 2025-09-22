import { Ionicons } from '@expo/vector-icons';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

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
    <View style={styles.container}>
      <View style={styles.iconContainer}>
        <Ionicons name={iconName} size={iconSize} color={iconColor} />
      </View>
      <Text style={styles.text}>{message}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  iconContainer: {
    marginBottom: 16,
  },
  text: {
    fontSize: 16,
    color: '#8E8E93',
    fontWeight: '500',
  },
});
