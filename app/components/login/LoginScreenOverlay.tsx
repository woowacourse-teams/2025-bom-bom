import styled from '@emotion/native';
import { Ionicons } from '@expo/vector-icons';
import React from 'react';
import { Modal } from 'react-native';

import { LoginScreen } from './LoginScreen';

interface LoginScreenOverlayProps {
  visible: boolean;
  onClose: () => void;
}

export const LoginScreenOverlay = ({
  visible,
  onClose,
}: LoginScreenOverlayProps) => {
  return (
    <Modal
      visible={visible}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={onClose}
    >
      <Container>
        <CloseButton
          onPress={onClose}
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          style={{
            shadowColor: '#000',
            shadowOffset: { width: 0, height: 2 },
            shadowOpacity: 0.1,
            shadowRadius: 4,
            elevation: 3,
          }}
        >
          <Ionicons name="close" size={24} color="#8E8E93" />
        </CloseButton>

        <LoginScreen />
      </Container>
    </Modal>
  );
};

const Container = styled.View`
  flex: 1;
  position: relative;
`;

const CloseButton = styled.TouchableOpacity`
  position: absolute;
  top: 60px;
  right: 20px;
  z-index: 1000;
  width: 32px;
  height: 32px;
  border-radius: 16px;
  background-color: #f2f2f7;
  justify-content: center;
  align-items: center;
`;
