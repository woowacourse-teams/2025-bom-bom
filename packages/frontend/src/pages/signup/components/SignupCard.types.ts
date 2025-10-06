import type { components } from '@/types/openapi';

export type Gender = components['schemas']['MemberSignupRequest']['gender'];

export type FieldError = string | null;
