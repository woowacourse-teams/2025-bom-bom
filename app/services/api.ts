import AsyncStorage from '@react-native-async-storage/async-storage';
import { LoginResponse } from '../types/auth';

const API_BASE_URL = 'https://api-dev.bombom.news/api/v1';

export class ApiClient {
  private static async getAuthHeaders(): Promise<Record<string, string>> {
    const token = await AsyncStorage.getItem('authToken');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    return headers;
  }

  private static async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let errorMessage = 'An error occurred';
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorMessage;
      } catch {
        errorMessage = `HTTP ${response.status}: ${response.statusText}`;
      }

      const error = new Error(errorMessage) as any;
      error.status = response.status;
      throw error;
    }

    return response.json();
  }

  // Google 로그인
  static async loginWithGoogle(
    idToken: string,
    serverAuthCode: string,
  ): Promise<LoginResponse> {
    try {
      const url = `${API_BASE_URL}/auth/login/google/native`;

      const response = await fetch(url, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          identityToken: idToken,
          authorizationCode: serverAuthCode,
        }),
      });

      return this.handleResponse<LoginResponse>(response);
    } catch (error) {
      console.error('Login with Google failed:', error);
      throw error;
    }
  }

  // Apple 로그인
  static async loginWithApple(
    identityToken: string,
    authorizationCode: string,
  ): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login/apple/native`, {
      method: 'POST',
      credentials: 'include',
      headers: await this.getAuthHeaders(),
      body: JSON.stringify({
        identityToken,
        authorizationCode,
      }),
    });

    console.log({ response });

    return this.handleResponse<LoginResponse>(response);
  }
}
