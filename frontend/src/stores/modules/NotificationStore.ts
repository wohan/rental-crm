import { api } from "../../api/client";
import type { NotificationPayload, NotificationRuntime } from "./StoreRuntime";

export class NotificationStore {
  constructor(private readonly root: NotificationRuntime) {}

  async saveNotificationSettings(payload: NotificationPayload) {
    await this.root.runAction("Сохраняем уведомления", "Настройки уведомлений сохранены", async () => {
      await api.put("/notifications/settings", payload);
      await this.root.loadAll();
    });
  }

  async sendDueNotifications() {
    await this.root.runAction("Отправляем уведомления", "Отправка уведомлений выполнена", async () => {
      await api.post("/notifications/send-due");
      await this.root.loadAll();
    });
  }
}
