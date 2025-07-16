import styled from '@emotion/styled';
import { ThemeProvider } from '@emotion/react';
import { theme } from './styles/theme';
import Header from './components/Header/Header';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <Container>
        <Header />
      </Container>
    </ThemeProvider>
  );
};

const Container = styled.div``;

export default App;
