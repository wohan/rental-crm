import { api, Id } from "../../api/client";
import type { BillingPayload, BillingRuntime } from "./StoreRuntime";

export class BillingStore {
  constructor(private readonly root: BillingRuntime) {}

  async exportWorkbook() {
    await this.root.runAction("Готовим экспорт", "Экспорт скачан", async () => {
      const response = await fetch(`${import.meta.env.VITE_API_URL ?? "/api"}/export/workbook`, {
        headers: this.root.token ? { Authorization: `Bearer ${this.root.token}` } : {}
      });
      if (!response.ok) throw new Error("Не удалось скачать экспорт");
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "rentcrm-export.xlsx";
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  async saveBillingSettings(payload: BillingPayload) {
    await this.root.runAction("Сохраняем оплату", "Настройки оплаты сохранены", async () => {
      await api.put("/billing/settings", payload);
      await this.root.loadAll();
    });
  }

  async createCheckout(tariff: string) {
    await this.root.runAction("Создаем счет", "Счет создан", async () => {
      await api.post("/billing/checkout", { tariff });
      await this.root.loadAll();
    });
  }

  async markInvoicePaid(id: Id) {
    await this.root.runAction("Применяем оплату", "Тариф обновлен", async () => {
      await api.post(`/billing/invoices/${id}/mark-paid`);
      await this.root.loadAll();
    });
  }
}
