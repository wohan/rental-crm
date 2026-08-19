export type Id = string;

const API_URL = import.meta.env.VITE_API_URL ?? "/api";

export class ApiClient {
  token = localStorage.getItem("rentcrm_token") ?? "";

  setToken(token: string) {
    this.token = token;
    localStorage.setItem("rentcrm_token", token);
  }

  clearToken() {
    this.token = "";
    localStorage.removeItem("rentcrm_token");
  }

  async request<T>(path: string, init: RequestInit = {}): Promise<T> {
    const isAuthRequest = path.startsWith("/auth/");
    const headers = new Headers(init.headers);
    headers.set("Content-Type", "application/json");
    if (this.token && !isAuthRequest) {
      headers.set("Authorization", `Bearer ${this.token}`);
    }
    const response = await fetch(`${API_URL}${path}`, {
      ...init,
      headers
    });
    if (!response.ok) {
      let message = "Ошибка запроса";
      const text = await response.text().catch(() => "");
      if (text) {
        try {
          const body = JSON.parse(text) as { error?: string; message?: string };
          message = body.error ?? body.message ?? message;
        } catch {
          message = text;
        }
      }
      if (!isAuthRequest && (response.status === 401 || response.status === 403)) {
        this.clearToken();
        message = "Сессия истекла, войдите заново";
      }
      throw new Error(message);
    }
    if (response.status === 204) return undefined as T;
    const text = await response.text();
    return text ? JSON.parse(text) as T : undefined as T;
  }

  get<T>(path: string) { return this.request<T>(path); }
  post<T>(path: string, body?: unknown) { return this.request<T>(path, { method: "POST", body: JSON.stringify(body ?? {}) }); }
  put<T>(path: string, body: unknown) { return this.request<T>(path, { method: "PUT", body: JSON.stringify(body) }); }
  delete(path: string) { return this.request<void>(path, { method: "DELETE" }); }

  async upload<T>(path: string, formData: FormData): Promise<T> {
    const response = await fetch(`${API_URL}${path}`, {
      method: "POST",
      headers: this.token ? { Authorization: `Bearer ${this.token}` } : {},
      body: formData
    });
    if (!response.ok) {
      const body = await response.json().catch(() => ({ error: "Ошибка загрузки файла" }));
      throw new Error(body.error ?? "Ошибка загрузки файла");
    }
    return await response.json() as T;
  }
}

export const api = new ApiClient();
