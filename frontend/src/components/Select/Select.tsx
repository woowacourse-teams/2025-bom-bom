import styled from '@emotion/styled';
import { ComponentProps, useState } from 'react';
import { SelectOption } from './Select.types';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';
import { theme } from '@/styles/theme';
import ChevronDownIcon from '#/assets/chevron-down.svg';
import ChevronUpIcon from '#/assets/chevron-up.svg';

interface SelectProps<T extends string | number> extends ComponentProps<'div'> {
  options: SelectOption<T>[];
  selectedValue: T | null;
  onSelectOption: (optionValue: T) => void;
  width?: number;
  placeholder?: string;
}

function Select<T extends string | number>({
  options,
  selectedValue,
  onSelectOption,
  width = 132,
  placeholder = '',
  ...props
}: SelectProps<T>) {
  const [open, setOpen] = useState(false);
  const selectRef = useClickOutsideRef<HTMLDivElement>(() => setOpen(false));
  const toggle = () => setOpen((prev) => !prev);

  const selectedLabel = options.find(
    (option) => option.value === selectedValue,
  )?.label;

  const onOptionSelected = (value: T) => {
    setOpen(false);
    onSelectOption(value);
  };

  return (
    <Container ref={selectRef} width={width} {...props}>
      <SelectToggle onClick={toggle}>
        <SelectText selected={selectedValue !== null}>
          {selectedLabel ?? placeholder}
        </SelectText>
        {open ? (
          <ChevronUpIcon width={16} color={theme.colors.black} />
        ) : (
          <ChevronDownIcon width={16} color={theme.colors.black} />
        )}
      </SelectToggle>
      <SelectMenu open={open}>
        <SelectMenuWrapper role="listbox">
          {options.map((option) => (
            <SelectMenuItem
              key={String(option.value)}
              onClick={() => onOptionSelected(option.value)}
              selected={option.value === selectedValue}
              role="option"
            >
              {option.label}
            </SelectMenuItem>
          ))}
        </SelectMenuWrapper>
      </SelectMenu>
    </Container>
  );
}

const Container = styled.div<{ width: number }>`
  position: relative;

  width: ${({ width }) => `${width}px`};
  height: 36px;
`;

const SelectToggle = styled.div`
  display: flex;
  gap: 10px;
  align-items: center;

  padding: 8px 12px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 6px;

  background-color: ${({ theme }) => theme.colors.white};

  box-sizing: border-box;
  cursor: pointer;
  user-select: none;
`;

const SelectText = styled.p<{ selected: boolean }>`
  overflow: hidden;

  flex: 1;

  color: ${({ theme, selected }) =>
    selected ? theme.colors.textPrimary : theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
  white-space: nowrap;

  text-overflow: ellipsis;
`;

const SelectMenu = styled.div<{ open: boolean }>`
  position: absolute;
  top: 100%;
  left: 0;
  z-index: ${({ theme }) => theme.zIndex.elevated};

  display: ${({ open }) => (open ? 'block' : 'none')};

  width: 100%;
  margin-top: 6px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 6px;
  box-shadow: 0 4px 6px 0 rgb(0 0 0 / 9%);

  background: white;

  box-sizing: border-box;
  list-style: none;
`;

const SelectMenuWrapper = styled.ul`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;

  width: 100%;
  padding: 4px;
  border-radius: 6px;

  background-color: ${({ theme }) => theme.colors.white};
`;

const SelectMenuItem = styled.li<{ selected: boolean }>`
  width: 100%;
  padding: 6px 8px;
  border-radius: 6px;

  background-color: ${({ theme, selected }) =>
    selected ? theme.colors.primaryLight : theme.colors.white};

  font: ${({ theme }) => theme.fonts.caption};

  cursor: pointer;

  &:hover {
    background-color: ${({ theme }) => theme.colors.primaryLight};
  }
`;

export default Select;
