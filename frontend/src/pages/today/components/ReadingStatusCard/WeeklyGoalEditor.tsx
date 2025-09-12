import styled from '@emotion/styled';
import { ChangeEvent, KeyboardEvent } from 'react';
import { DeviceType } from '@/hooks/useDeviceType';
import EditIcon from '#/assets/edit.svg';

interface WeeklyGoalEditorProps {
  isEditing: boolean;
  goalValue: number | null;
  isPending: boolean;
  deviceType: DeviceType;
  onEditStart: () => void;
  onSave: () => void;
  onCancel: () => void;
  onGoalChange: (value: number | null) => void;
}

function WeeklyGoalEditor({
  isEditing,
  goalValue,
  isPending,
  deviceType,
  onEditStart,
  onSave,
  onCancel,
  onGoalChange,
}: WeeklyGoalEditorProps) {
  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value === '' || /^[1-9][0-9]{0,2}$/.test(value)) {
      onGoalChange(Number(value) || null);
    }
  };

  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Enter') {
      onSave();
    } else if (e.key === 'Escape') {
      onCancel();
    }
  };

  return (
    <EditSection deviceType={deviceType}>
      {isEditing ? (
        <EditInput
          deviceType={deviceType}
          type="text"
          value={goalValue || ''}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          onBlur={onSave}
          // eslint-disable-next-line jsx-a11y/no-autofocus
          autoFocus
          disabled={isPending}
          placeholder="1-127"
          aria-label="주간 목표 수정"
        />
      ) : (
        <EditButton
          deviceType={deviceType}
          type="button"
          onClick={onEditStart}
          aria-label="주간 목표 편집"
          disabled={isPending}
        >
          <EditIcon
            width={deviceType === 'pc' ? 16 : 14}
            height={deviceType === 'pc' ? 16 : 14}
          />
        </EditButton>
      )}
    </EditSection>
  );
}

export default WeeklyGoalEditor;

const EditSection = styled.div<{ deviceType: DeviceType }>`
  position: absolute;
  ${({ deviceType }) =>
    deviceType === 'pc'
      ? `
      top: 0;
      right: -20px;
      height: 22px;
    `
      : `
      top: -2px;
      right: 0;
      height: 20px;
    `}

  display: flex;
  align-items: center;
`;

const EditButton = styled.button<{ deviceType: DeviceType }>`
  ${({ deviceType }) =>
    deviceType === 'pc'
      ? `
        width: 24px;
        height: 24px;
        padding: 4px;
      `
      : `
        width: 20px;
        height: 20px;
        padding: 3px;
      `}
  border: none;
  border-radius: 4px;

  display: flex;
  align-items: center;
  justify-content: center;

  background: transparent;
  color: ${({ theme }) => theme.colors.textTertiary};

  cursor: pointer;

  transition: all 0.2s ease;

  &:focus {
    outline: 2px solid ${({ theme }) => theme.colors.primary};
    outline-offset: 2px;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  svg {
    fill: currentcolor;
  }
`;

const EditInput = styled.input<{ deviceType: DeviceType }>`
  ${({ deviceType }) =>
    deviceType === 'pc'
      ? `
        width: 60px;
        height: 24px;
        padding: 2px 8px;
      `
      : `
        width: 50px;
        height: 20px;
        padding: 2px 6px;
      `}
  border: 1px solid ${({ theme }) => theme.colors.primary};
  border-radius: 4px;

  background-color: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, deviceType }) =>
    deviceType === 'pc' ? theme.fonts.body2 : theme.fonts.caption};
  text-align: center;

  &:focus {
    outline: none;
    box-shadow: 0 0 0 2px ${({ theme }) => theme.colors.primary}20;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors.textTertiary};
  }
`;
