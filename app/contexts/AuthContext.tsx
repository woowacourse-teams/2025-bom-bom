import React, {
  createContext,
  PropsWithChildren,
  useContext,
  useState,
} from 'react';

export interface AuthContextType {
  showWebViewLogin: boolean;
  setShowWebViewLogin: (show: boolean) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [showWebViewLogin, setShowWebViewLogin] = useState(false);

  return (
    <AuthContext.Provider
      value={{
        showWebViewLogin,
        setShowWebViewLogin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
