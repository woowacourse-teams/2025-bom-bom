import AsyncStorage from "@react-native-async-storage/async-storage";
import { LoginResponse } from "../types/auth";

const API_BASE_URL = "https://api-dev.bombom.news/api/v1";

export class ApiClient {
  private static async getAuthHeaders(): Promise<Record<string, string>> {
    const token = await AsyncStorage.getItem("authToken");
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    return headers;
  }

  private static async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let errorMessage = "An error occurred";
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
  static async loginWithGoogle(idToken: string): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login/google/native`, {
      method: "POST",
      headers: await this.getAuthHeaders(),
      body: JSON.stringify({ idToken }),
    });

    return this.handleResponse<LoginResponse>(response);
  }

  // Apple 로그인
  static async loginWithApple(
    identityToken: string,
    authorizationCode: string
  ): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login/apple/native`, {
      method: "POST",
      headers: await this.getAuthHeaders(),
      body: JSON.stringify({
        identityToken,
        authorizationCode,
      }),
    });

    return this.handleResponse<LoginResponse>(response);
  }

  // 로그아웃 (서버에 토큰 무효화 요청)
  static async logout(): Promise<void> {
    try {
      await fetch(`${API_BASE_URL}/auth/logout`, {
        method: "POST",
        headers: await this.getAuthHeaders(),
      });
    } catch (error) {
      // 로그아웃 요청이 실패해도 로컬 토큰은 삭제해야 함
      console.warn("Logout request failed:", error);
    }
  }

  // 토큰 검증
  static async verifyToken(): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/verify`, {
      method: "GET",
      headers: await this.getAuthHeaders(),
    });

    return this.handleResponse<LoginResponse>(response);
  }
}
