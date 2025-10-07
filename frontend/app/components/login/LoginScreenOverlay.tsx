import styled from '@emotion/native';
import { Ionicons } from '@expo/vector-icons';

import { Modal } from 'react-native';

import { theme } from 'shared/theme';
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
        >
          <Ionicons name="close" size={24} color={theme.colors.icons} />
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
  background-color: ${(props) => props.theme.colors.disabledBackground};
  justify-content: center;
  align-items: center;
`;
