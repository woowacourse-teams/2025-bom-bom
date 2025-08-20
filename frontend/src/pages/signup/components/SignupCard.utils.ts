import { FieldError, Gender } from './SignupCard.types';

const nicknameRegex = /^[가-힣a-zA-Z0-9_]{2,12}$/; // 2-12자, 허용 문자만
const emailLocalRegex = /^[a-zA-Z0-9](?:[a-zA-Z0-9._-]{0,62}[a-zA-Z0-9])?$/; // 시작/끝 영숫자
const consecutiveDots = /\.\./;

export const validateNickname = (nickname: string): FieldError => {
  const v = nickname.trim();
  if (!v) return '닉네임을 입력해주세요.';
  if (!nicknameRegex.test(v))
    return '닉네임은 2–12자, 한글/영문/숫자/밑줄만 가능합니다.';
  return null;
};

export const validateEmailLocal = (emailLocal: string): FieldError => {
  const v = emailLocal.trim();
  if (!v) return '이메일을 입력해주세요.';
  if (v.length > 64) return '이메일 로컬파트 길이는 64자 이하여야 합니다.';
  if (consecutiveDots.test(v))
    return '이메일에 연속된 마침표(..)는 사용할 수 없습니다.';
  if (!emailLocalRegex.test(v)) return '이메일 형식이 올바르지 않습니다.';
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
  const y = Number(yStr);
  const m = Number(mStr);
  const d = Number(dStr);

  const dt = new Date(trimmedInput);
  const isValid =
    !Number.isNaN(dt.getTime()) &&
    dt.getUTCFullYear() === y &&
    dt.getUTCMonth() + 1 === m &&
    dt.getUTCDate() === d;
  if (!isValid) return '존재하지 않는 날짜입니다.';

  const today = new Date();
  const todayYMD = new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate(),
  ).getTime();
  const dateYMD = new Date(y, m - 1, d).getTime();
  if (dateYMD > todayYMD) return '미래 날짜는 입력할 수 없습니다.';

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
