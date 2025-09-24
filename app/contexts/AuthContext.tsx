import React, {
  createContext,
  PropsWithChildren,
  useContext,
  useState,
} from 'react';

export interface AuthContextType {
  showWebViewLogin: boolean;
  showLogin: () => void;
  hideLogin: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [showWebViewLogin, setShowWebViewLogin] = useState(false);

  const showLogin = () => {
    setShowWebViewLogin(true);
  };

  const hideLogin = () => {
    setShowWebViewLogin(false);
  };

  return (
    <AuthContext.Provider
      value={{
        showWebViewLogin,
        showLogin,
        hideLogin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
