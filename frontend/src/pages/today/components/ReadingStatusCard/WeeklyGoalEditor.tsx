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

interface WeeklyGoalInputProps {
  goalValue: number | null;
  isPending: boolean;
  deviceType: DeviceType;
  onSave: () => void;
  onCancel: () => void;
  onGoalChange: (value: number | null) => void;
}

export function WeeklyGoalInput({
  goalValue,
  isPending,
  deviceType,
  onSave,
  onCancel,
  onGoalChange,
}: WeeklyGoalInputProps) {
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
    <EditInput
      deviceType={deviceType}
      type="text"
      // eslint-disable-next-line jsx-a11y/no-autofocus
      autoFocus
      autoComplete="off"
      value={goalValue || ''}
      onChange={handleInputChange}
      onKeyDown={handleKeyDown}
      onBlur={onSave}
      disabled={isPending}
      placeholder="1-127"
      aria-label="주간 목표 수정"
    />
  );
}

function WeeklyGoalEditor({
  isPending,
  deviceType,
  onEditStart,
}: Pick<WeeklyGoalEditorProps, 'isPending' | 'deviceType' | 'onEditStart'>) {
  return (
    <EditSection deviceType={deviceType}>
      <EditButton
        deviceType={deviceType}
        type="button"
        onClick={onEditStart}
        aria-label="주간 목표 편집"
        disabled={isPending}
      >
        <EditIcon
          width={deviceType === 'pc' ? 14 : 12}
          height={deviceType === 'pc' ? 14 : 12}
        />
      </EditButton>
    </EditSection>
  );
}

export default WeeklyGoalEditor;

const EditSection = styled.section<{ deviceType: DeviceType }>`
  margin-left: 4px;

  display: flex;
  align-items: center;
`;

const EditButton = styled.button<{ deviceType: DeviceType }>`
  ${({ deviceType }) =>
    deviceType === 'pc'
      ? `
        width: 20px;
        height: 20px;
        padding: 2px;
      `
      : `
        width: 18px;
        height: 18px;
        padding: 2px;
      `}
  border: none;
  border-radius: 3px;

  display: flex;
  align-items: center;
  justify-content: center;

  background: transparent;
  color: ${({ theme }) => theme.colors.textTertiary};

  cursor: pointer;

  transition: all 0.2s ease;

  &:hover {
    background: ${({ theme }) => theme.colors.dividers};
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
  width: 40px;
  height: auto;
  margin: 0;
  padding: 2px 4px;
  border: 1px solid ${({ theme }) => theme.colors.primary};
  border-radius: 3px;

  background-color: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};
  line-height: 1;
  text-align: center;

  &:focus {
    outline: none;
    box-shadow: 0 0 0 1px ${({ theme }) => theme.colors.primary}40;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors.textTertiary};
    font-size: 10px;
  }
`;
