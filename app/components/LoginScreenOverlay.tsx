import { Ionicons } from "@expo/vector-icons";
import React from "react";
import { Modal, StyleSheet, TouchableOpacity, View } from "react-native";

import { LoginScreen } from "./LoginScreen";

interface LoginScreenOverlayProps {
  visible: boolean;
  onClose: () => void;
}

export const LoginScreenOverlay: React.FC<LoginScreenOverlayProps> = ({
  visible,
  onClose,
}) => {
  return (
    <Modal
      visible={visible}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={onClose}
    >
      <View style={styles.container}>
        <TouchableOpacity
          style={styles.closeButton}
          onPress={onClose}
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
        >
          <Ionicons name="close" size={24} color="#8E8E93" />
        </TouchableOpacity>

        <LoginScreen />
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    position: "relative",
  },
  closeButton: {
    position: "absolute",
    top: 60,
    right: 20,
    zIndex: 1000,
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: "#F2F2F7",
    justifyContent: "center",
    alignItems: "center",
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
});
