import { FieldError, Gender } from './SignupCard.types';

const nicknameRegex = /^[가-힣a-zA-Z0-9_]{2,12}$/;
const emailLocalRegex = /^[A-Za-z0-9._-]{3,30}$/;
const consecutiveDots = /\.\./;

export const validateNickname = (nickname: string): FieldError => {
  const trimmedNickname = nickname.trim();
  if (!trimmedNickname) return '닉네임을 입력해주세요.';
  if (!nicknameRegex.test(trimmedNickname))
    return '닉네임은 2–12자, 한글/영문/숫자/밑줄만 가능합니다.';
  return null;
};

export const validateEmailLocal = (emailLocal: string): FieldError => {
  const trimmedEmailLocal = emailLocal.trim();
  if (!trimmedEmailLocal) return '이메일을 입력해주세요.';
  if (trimmedEmailLocal.length > 64)
    return '이메일 로컬파트 길이는 64자 이하여야 합니다.';
  if (consecutiveDots.test(trimmedEmailLocal))
    return '이메일에 연속된 마침표(..)는 사용할 수 없습니다.';
  if (!emailLocalRegex.test(trimmedEmailLocal))
    return '이메일 형식이 올바르지 않습니다.';
  return null;
};

export const validateBirthDate = (input: string): FieldError => {
  const trimmedInput = input.trim();
  if (!trimmedInput) return '생년월일을 입력해주세요.';
  if (trimmedInput.length < 7 || trimmedInput[4] !== '-') {
    return '생년월일 형식이 올바르지 않습니다. 예) 1999-01-23';
  }
  if (
    trimmedInput.length !== 10 ||
    trimmedInput[4] !== '-' ||
    trimmedInput[7] !== '-'
  ) {
    return '생년월일은 YYYY-MM-DD 형식이어야 합니다.';
  }

  const [yStr, mStr, dStr] = trimmedInput.split('-');
  const year = Number(yStr);
  const month = Number(mStr);
  const day = Number(dStr);

  const inputDate = new Date(trimmedInput);
  const isValid =
    !Number.isNaN(inputDate.getTime()) &&
    inputDate.getUTCFullYear() === year &&
    inputDate.getUTCMonth() + 1 === month &&
    inputDate.getUTCDate() === day;
  if (!isValid) return '존재하지 않는 날짜입니다.';

  const today = Date.now();
  const inputTime = inputDate.getTime();
  if (inputTime > today) return '미래 날짜는 입력할 수 없습니다.';

  return null;
};

export const validateGender = (gender: Gender | null): FieldError => {
  if (!gender) return '성별을 선택해주세요.';
  return null;
};

export const formatBirthDate = (input: string): string => {
  const digits = input.replace(/\D/g, '').slice(0, 8);
  if (digits.length <= 4) return digits;
  if (digits.length <= 6) return `${digits.slice(0, 4)}-${digits.slice(4, 6)}`;
  return `${digits.slice(0, 4)}-${digits.slice(4, 6)}-${digits.slice(6, 8)}`;
};
