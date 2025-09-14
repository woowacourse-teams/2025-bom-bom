import styled from '@emotion/styled';
import { ChangeEvent, KeyboardEvent } from 'react';
import { DeviceType } from '@/hooks/useDeviceType';
import CheckIcon from '#/assets/check.svg';
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
      onFocus={(e) => {
        e.target.select();
      }}
    />
  );
}

function WeeklyGoalEditor({
  isEditing,
  isPending,
  deviceType,
  onEditStart,
  onSave,
}: Pick<
  WeeklyGoalEditorProps,
  'isEditing' | 'isPending' | 'deviceType' | 'onEditStart' | 'onSave'
>) {
  return (
    <Container deviceType={deviceType}>
      <EditButton
        deviceType={deviceType}
        type="button"
        onClick={isEditing ? onSave : onEditStart}
        aria-label={isEditing ? '주간 목표 저장' : '주간 목표 편집'}
        disabled={isPending}
        isEditing={isEditing}
      >
        {isEditing ? (
          <>
            <CheckIcon width={12} height={12} />
            {deviceType === 'pc' && (
              <ButtonText deviceType={deviceType}>수정 완료</ButtonText>
            )}
          </>
        ) : (
          <EditIcon
            width={deviceType === 'pc' ? 14 : 12}
            height={deviceType === 'pc' ? 14 : 12}
          />
        )}
      </EditButton>
    </Container>
  );
}

export default WeeklyGoalEditor;

const Container = styled.div<{ deviceType: DeviceType }>`
  margin-left: 4px;

  display: flex;
  align-items: center;
`;

const EditButton = styled.button<{
  deviceType: DeviceType;
  isEditing?: boolean;
}>`
  border: none;
  border-radius: 3px;

  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;

  background: ${({ theme, isEditing }) =>
    isEditing ? theme.colors.primary : 'transparent'};
  color: ${({ theme, isEditing }) =>
    isEditing ? theme.colors.white : theme.colors.textTertiary};

  cursor: pointer;

  transition: all 0.2s ease;

  &:hover {
    background: ${({ theme, isEditing }) =>
      isEditing ? theme.colors.primary : theme.colors.dividers};
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  svg {
    fill: currentcolor;
  }

  ${({ deviceType, isEditing }) =>
    deviceType === 'pc'
      ? `
        width: ${isEditing ? 'auto' : '20px'};
        height: 20px;
        padding: ${isEditing ? '4px 8px' : '2px'};
        min-width: ${isEditing ? '72px' : '20px'};
      `
      : `
        width: ${isEditing ? 'auto' : '18px'};
        height: 18px;
        padding: ${isEditing ? '3px 6px' : '2px'};
        min-width: 18px;
      `}
`;

const ButtonText = styled.span<{ deviceType: DeviceType }>`
  font: ${({ theme, deviceType }) =>
    deviceType === 'pc' ? theme.fonts.caption : theme.fonts.caption};
  font-size: ${({ deviceType }) => (deviceType === 'pc' ? '11px' : '10px')};
  white-space: nowrap;
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
