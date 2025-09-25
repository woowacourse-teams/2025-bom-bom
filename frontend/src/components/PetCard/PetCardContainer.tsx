import styled from '@emotion/styled';
import { useDevice } from '@/hooks/useDevice';
import { theme } from '@/styles/theme';
import type { Device } from '@/hooks/useDevice';
import type { CSSObject, Theme } from '@emotion/react';
import type { PropsWithChildren } from 'react';
import PetIcon from '#/assets/svg/pet.svg';

const PetCardContainer = ({ children }: PropsWithChildren) => {
  const device = useDevice();

  return (
    <Container device={device}>
      {device === 'pc' && (
        <TitleWrapper>
          <StatusIconWrapper>
            <PetIcon width={16} height={16} color={theme.colors.white} />
          </StatusIconWrapper>
          <Title>봄이</Title>
        </TitleWrapper>
      )}
      {children}
    </Container>
  );
};

export default PetCardContainer;

const Container = styled.section<{ device: Device }>`
  width: 310px;
  border-radius: 20px;

  display: flex;
  gap: 16px;
  flex-direction: column;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  ${({ device, theme }) => containerStyles[device](theme)}
`;

const TitleWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 10px;
  align-items: center;
`;

const StatusIconWrapper = styled.div`
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 14px;

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h2`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
  text-align: center;
`;

const containerStyles: Record<Device, (theme: Theme) => CSSObject> = {
  pc: () => ({
    padding: '34px 30px',
    border: `1px solid ${theme.colors.white}`,
    boxShadow: '0 25px 50px -12px rgb(0 0 0 / 15%)',
    backgroundColor: theme.colors.white,
  }),
  tablet: () => ({
    flex: '1',
  }),
  mobile: () => ({
    flex: '1',
  }),
};
