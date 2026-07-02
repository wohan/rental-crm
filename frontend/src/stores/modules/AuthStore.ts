import { api } from "../../api/client";
import type { AuthRuntime, LoginPayload, RegisterPayload } from "./StoreRuntime";

export class AuthStore {
  constructor(private readonly root: AuthRuntime) {}

  async register(payload: RegisterPayload) {
    await this.root.runAction("Создаем кабинет", "Кабинет создан", async () => {
      const res = await api.post<{ token: string; verificationLink?: string }>("/auth/register", payload);
      api.setToken(res.token);
      this.root.token = res.token;
      this.root.verificationLink = res.verificationLink ?? "";
      await this.root.loadAll();
    });
  }

  async login(payload: LoginPayload) {
    await this.root.runAction("Входим", "Вход выполнен", async () => {
      const res = await api.post<{ token: string }>("/auth/login", payload);
      api.setToken(res.token);
      this.root.token = res.token;
      await this.root.loadAll();
    });
  }

  async forgotPassword(email: string) {
    await this.root.runAction("Создаем ссылку", "Инструкция для сброса пароля создана", async () => {
      const res = await api.post<{ resetLink?: string }>("/auth/forgot-password", { email });
      this.root.resetLink = res.resetLink ?? "";
    });
  }

  async resetPassword(token: string, newPassword: string) {
    await this.root.runAction("Меняем пароль", "Пароль изменен", async () => {
      await api.post("/auth/reset-password", { token, newPassword });
      this.root.resetLink = "";
    });
  }

  logout() {
    api.clearToken();
    this.root.token = "";
    this.root.user = null;
    this.root.verificationLink = "";
    this.root.resetLink = "";
  }
}
