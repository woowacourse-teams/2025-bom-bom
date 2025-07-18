import { ThemeProvider } from '@emotion/react';
import styled from '@emotion/styled';
import { theme } from './styles/theme';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <Container>Hello World</Container>
    </ThemeProvider>
  );
};

const Container = styled.div`
  position: relative;
  z-index: 0;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  margin: 0;
  padding: 20px;

  background-color: #f0f0f0;
  font-family: Arial, sans-serif;
`;

export default App;
