import type { components } from '@/types/openapi';

export type Gender = components['schemas']['MemberSignupRequest']['gender'];

// FieldError는 이제 useUserInfoValidation 훅에서 가져옵니다
export type { FieldError } from '@/hooks/useUserInfoValidation';
