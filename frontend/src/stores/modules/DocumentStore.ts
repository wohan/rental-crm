import { api } from "../../api/client";
import type { DocumentItem } from "../types";
import type { DocumentDownload, DocumentRuntime } from "./StoreRuntime";

export class DocumentStore {
  constructor(private readonly root: DocumentRuntime) {}

  async uploadDocument(formData: FormData) {
    await this.root.runAction("Загружаем документ", "Документ загружен", async () => {
      await api.upload<DocumentItem>("/documents/upload", formData);
      await this.root.loadAll();
    });
  }

  async downloadDocument(document: DocumentDownload) {
    await this.root.runAction("Скачиваем документ", "Документ скачан", async () => {
      const response = await fetch(`${import.meta.env.VITE_API_URL ?? "/api"}/documents/${document.id}/file`, {
        headers: this.root.token ? { Authorization: `Bearer ${this.root.token}` } : {}
      });
      if (!response.ok) {
        const body = await response.json().catch(() => ({ error: "Не удалось скачать документ" }));
        throw new Error(body.error ?? "Не удалось скачать документ");
      }
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = window.document.createElement("a");
      link.href = url;
      link.download = document.originalFileName ?? document.name;
      link.click();
      URL.revokeObjectURL(url);
    });
  }
}
