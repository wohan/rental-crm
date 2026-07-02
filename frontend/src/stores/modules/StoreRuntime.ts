import type { Id } from "../../api/client";
import type { BillingSettings, DocumentItem, NotificationSettings } from "../types";

export type StoreRuntime = {
  token: string;
  verificationLink: string;
  resetLink: string;
  user: unknown;
  runAction(label: string, success: string, action: () => Promise<void>): Promise<void>;
  loadAll(): Promise<void>;
};

export type AuthRuntime = StoreRuntime & {
  token: string;
  verificationLink: string;
  resetLink: string;
  user: unknown;
};

export type DocumentRuntime = StoreRuntime;
export type NotificationRuntime = StoreRuntime;
export type BillingRuntime = StoreRuntime;
export type CrudRuntime = StoreRuntime;

export type RegisterPayload = { email: string; password: string; fullName: string; accountName: string; termsAccepted?: boolean; privacyAccepted?: boolean; notificationConsent?: boolean };
export type LoginPayload = { email: string; password: string };
export type EntityPath = string;
export type EntityId = Id;
export type DocumentDownload = DocumentItem;
export type NotificationPayload = NotificationSettings;
export type BillingPayload = BillingSettings;
