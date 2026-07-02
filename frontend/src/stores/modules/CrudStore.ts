import { api } from "../../api/client";
import type { Contract } from "../types";
import type { CrudRuntime, EntityId, EntityPath } from "./StoreRuntime";

export class CrudStore {
  constructor(private readonly root: CrudRuntime) {}

  async create(path: EntityPath, payload: unknown) {
    await this.root.runAction("Сохраняем", "Сохранено", async () => {
      await api.post(path, payload);
      await this.root.loadAll();
    });
  }

  async createContract(payload: Record<string, unknown>, file?: File | null) {
    await this.root.runAction("Сохраняем", "Сохранено", async () => {
      const contract = await api.post<Contract>("/contracts", payload);
      if (file) {
        const formData = new FormData();
        formData.append("objectId", String(contract.objectId));
        formData.append("tenantId", String(contract.tenantId));
        formData.append("contractId", String(contract.id));
        formData.append("name", `Договор № ${contract.number}`);
        formData.append("type", "CONTRACT");
        formData.append("expiresAt", contract.endDate);
        formData.append("file", file);
        await api.upload("/documents/upload", formData);
      }
      await this.root.loadAll();
    });
  }

  async update(path: EntityPath, payload: unknown, success = "Изменения сохранены") {
    await this.root.runAction("Сохраняем", success, async () => {
      await api.put(path, payload);
      await this.root.loadAll();
    });
  }

  async remove(path: EntityPath) {
    await this.root.runAction("Удаляем", "Запись удалена", async () => {
      await api.delete(path);
      await this.root.loadAll();
    });
  }

  async markPaymentPaid(id: EntityId) {
    await this.root.runAction("Отмечаем платеж", "Платеж отмечен оплаченным", async () => {
      await api.post(`/payments/${id}/mark-paid`);
      await this.root.loadAll();
    });
  }
}
